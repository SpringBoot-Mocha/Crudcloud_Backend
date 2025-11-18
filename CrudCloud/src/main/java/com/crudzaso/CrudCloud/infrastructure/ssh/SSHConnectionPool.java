package com.crudzaso.CrudCloud.infrastructure.ssh;

import lombok.extern.slf4j.Slf4j;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * SSH Connection Pool for connecting to the CrudCloud VPS at 91.98.225.17
 *
 * Handles SSH connections for:
 * - Creating Docker containers
 * - Creating databases
 * - Managing database users and permissions
 * - Executing remote commands
 *
 * IMPORTANT: This component is responsible for all remote operations on the VPS.
 */
@Component
@Slf4j
public class SSHConnectionPool {

    @Value("${ssh.host:91.98.225.17}")
    private String sshHost;

    @Value("${ssh.port:22}")
    private int sshPort;

    @Value("${ssh.username}")
    private String sshUsername;

    @Value("${ssh.password:}")
    private String sshPassword;

    @Value("${ssh.private-key-path:~/.ssh/id_rsa}")
    private String sshPrivateKeyPath;

    @Value("${ssh.connection-timeout:30000}")
    private long connectionTimeout;

    @Value("${ssh.read-timeout:30000}")
    private long readTimeout;

    /**
     * Execute a command on the remote VPS via SSH
     * Returns the standard output of the command
     * Uses default timeout of 5 seconds
     *
     * @param command The shell command to execute on VPS
     * @return The command output (stdout)
     * @throws SSHException if connection or execution fails
     */
    public String executeCommand(String command) throws SSHException {
        return executeCommandInternal(command, 5);  // Default 5 seconds for backward compatibility
    }

    /**
     * Execute a command on the remote VPS via SSH with timeout of 30 seconds
     * Used for provisioning commands like Docker operations
     *
     * @param command The shell command to execute on VPS
     * @return The command output (stdout)
     * @throws SSHException if execution fails
     */
    public String executeCommandWithProgressiveRetries(String command) throws SSHException {
        // Using 30 seconds timeout for all provisioning operations
        return executeCommandInternal(command, 30);
    }

    /**
     * Internal method that executes SSH command with specified timeout
     *
     * @param command The shell command to execute on VPS
     * @param timeoutSeconds Command execution timeout in seconds
     * @return The command output (stdout)
     * @throws SSHException if connection or execution fails
     */
    private String executeCommandInternal(String command, int timeoutSeconds) throws SSHException {
        log.info("Executing SSH command on {}@{} (timeout: {}s)", sshUsername, sshHost, timeoutSeconds);
        log.debug("Command: {}", command);

        SSHClient sshClient = new SSHClient();
        try {
            // Configure SSH client
            sshClient.addHostKeyVerifier(new PromiscuousVerifier());
            sshClient.setConnectTimeout((int) connectionTimeout);
            sshClient.setTimeout((int) readTimeout);

            // Connect to VPS
            log.debug("Connecting to SSH server at {}:{}", sshHost, sshPort);
            sshClient.connect(sshHost, sshPort);

            // Try SSH key authentication first (most secure)
            String expandedKeyPath = sshPrivateKeyPath.replace("~", System.getProperty("user.home"));
            java.io.File keyFile = new java.io.File(expandedKeyPath);

            if (keyFile.exists()) {
                try {
                    log.debug("Attempting SSH key authentication with: {}", expandedKeyPath);
                    sshClient.loadKnownHosts();
                    sshClient.authPublickey(sshUsername, expandedKeyPath);
                    log.info("✅ SSH key authentication successful");
                } catch (IOException e) {
                    log.warn("SSH key authentication failed: {}, trying password authentication", e.getMessage());
                    // Fall through to password authentication
                    if (sshPassword != null && !sshPassword.isEmpty()) {
                        log.debug("Attempting password authentication");
                        sshClient.authPassword(sshUsername, sshPassword);
                        log.info("✅ Password authentication successful");
                    } else {
                        throw new SSHException("SSH key authentication failed and no password provided");
                    }
                }
            } else {
                // Key file doesn't exist, use password authentication
                if (sshPassword != null && !sshPassword.isEmpty()) {
                    log.debug("SSH key not found at {}, using password authentication", expandedKeyPath);
                    sshClient.authPassword(sshUsername, sshPassword);
                    log.info("✅ Password authentication successful");
                } else {
                    throw new SSHException("SSH key not found and no password provided");
                }
            }

            // Execute command
            log.debug("Executing command");
            Session session = sshClient.startSession();
            Session.Command cmd = session.exec(command);

            // Read output
            String output = IOUtils.readFully(cmd.getInputStream()).toString();
            String error = IOUtils.readFully(cmd.getErrorStream()).toString();

            // Wait for command to complete with configurable timeout
            cmd.join(timeoutSeconds, TimeUnit.SECONDS);
            int exitStatus = cmd.getExitStatus();

            session.close();

            if (exitStatus != 0) {
                log.warn("SSH command exited with status: {}", exitStatus);
                log.warn("Error output: {}", error);
                throw new SSHException("Command failed with exit code " + exitStatus + ": " + error);
            }

            log.info("✅ SSH command executed successfully in {}s", timeoutSeconds);
            log.debug("Output length: {} bytes", output.length());

            return output;

        } catch (IOException e) {
            log.error("SSH connection error: {}", e.getMessage(), e);
            throw new SSHException("SSH connection failed: " + e.getMessage(), e);
        } finally {
            try {
                sshClient.close();
            } catch (IOException e) {
                log.warn("Error closing SSH connection: {}", e.getMessage());
            }
        }
    }

    /**
     * Test SSH connection without executing any command
     * Useful for health checks and validation
     *
     * @return true if connection is successful
     */
    public boolean testConnection() {
        log.info("Testing SSH connection to {}@{}", sshUsername, sshHost);

        SSHClient sshClient = new SSHClient();
        try {
            sshClient.addHostKeyVerifier(new PromiscuousVerifier());
            sshClient.setConnectTimeout((int) connectionTimeout);
            sshClient.connect(sshHost, sshPort);

            // Try SSH key authentication first
            String expandedKeyPath = sshPrivateKeyPath.replace("~", System.getProperty("user.home"));
            java.io.File keyFile = new java.io.File(expandedKeyPath);

            if (keyFile.exists()) {
                try {
                    sshClient.loadKnownHosts();
                    sshClient.authPublickey(sshUsername, expandedKeyPath);
                    log.info("✅ SSH connection test successful (using key authentication)");
                } catch (IOException e) {
                    log.warn("SSH key auth failed, trying password: {}", e.getMessage());
                    if (sshPassword != null && !sshPassword.isEmpty()) {
                        sshClient.authPassword(sshUsername, sshPassword);
                        log.info("✅ SSH connection test successful (using password authentication)");
                    } else {
                        return false;
                    }
                }
            } else {
                if (sshPassword != null && !sshPassword.isEmpty()) {
                    sshClient.authPassword(sshUsername, sshPassword);
                    log.info("✅ SSH connection test successful (using password authentication)");
                } else {
                    return false;
                }
            }

            sshClient.close();
            return true;
        } catch (IOException e) {
            log.error("❌ SSH connection test failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Custom exception for SSH operations
     */
    public static class SSHException extends Exception {
        public SSHException(String message) {
            super(message);
        }

        public SSHException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
