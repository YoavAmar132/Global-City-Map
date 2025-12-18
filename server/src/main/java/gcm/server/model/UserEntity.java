package gcm.server.model;

import java.sql.Timestamp;

public class UserEntity {

    private final int id;
    private final String username;
    private final String passwordHash;
    private final String role;
    private final int failedAttempts;
    private final boolean isLocked;
    private final Timestamp lockedUntil;

    public UserEntity(
            int id,
            String username,
            String passwordHash,
            String role,
            int failedAttempts,
            boolean isLocked,
            Timestamp lockedUntil
    ) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;   // do we really need to bother with this
        this.role = role;
        this.failedAttempts = failedAttempts;
        this.isLocked = isLocked;
        this.lockedUntil = lockedUntil;
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getRole() { return role; }
    public int getFailedAttempts() { return failedAttempts; }
    public boolean isLocked() { return isLocked; }
    public Timestamp getLockedUntil() { return lockedUntil; }
}
