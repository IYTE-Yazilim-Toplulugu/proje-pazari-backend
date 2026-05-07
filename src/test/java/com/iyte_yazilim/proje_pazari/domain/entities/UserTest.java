package com.iyte_yazilim.proje_pazari.domain.entities;

import static org.junit.jupiter.api.Assertions.*;

import com.iyte_yazilim.proje_pazari.domain.enums.RoleType;
import com.iyte_yazilim.proje_pazari.domain.exceptions.IllegalUserStateException;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("test@std.iyte.edu.tr", "hashedPassword", "John", "Doe");
    }

    // ---------------------------------------------------------------------------
    //  Constructor & Defaults
    // ---------------------------------------------------------------------------

    @Test
    @DisplayName("Default constructor sets isActive=true and empty roles")
    void defaultConstructor_setsActiveAndEmptyRoles() {
        User u = new User();
        assertTrue(u.isActive());
        assertNotNull(u.getRoles());
        assertTrue(u.getRoles().isEmpty());
    }

    @Test
    @DisplayName("Parameterized constructor sets all fields correctly")
    void parameterizedConstructor_setsFieldsCorrectly() {
        assertEquals("test@std.iyte.edu.tr", user.getEmail());
        assertEquals("hashedPassword", user.getPassword());
        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertTrue(user.isActive());
    }

    // ---------------------------------------------------------------------------
    //  getFullName()
    // ---------------------------------------------------------------------------

    @Test
    @DisplayName("getFullName returns 'FirstName LastName'")
    void getFullName_returnsCombinedName() {
        assertEquals("John Doe", user.getFullName());
    }

    // ---------------------------------------------------------------------------
    //  Lifecycle: activate() / deactivate()
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("activate()")
    class ActivateTests {

        @Test
        @DisplayName("Activates an inactive user")
        void activate_onInactiveUser_succeeds() {
            user.reconstituteActive(false); // Infrastructure reconstitution to set inactive state
            assertFalse(user.isActive());

            user.activate();

            assertTrue(user.isActive());
        }

        @Test
        @DisplayName("Throws IllegalUserStateException on already active user")
        void activate_onActiveUser_throwsException() {
            assertTrue(user.isActive());

            IllegalUserStateException ex =
                    assertThrows(IllegalUserStateException.class, () -> user.activate());
            assertTrue(ex.getMessage().contains("already active"));
        }
    }

    @Nested
    @DisplayName("deactivate()")
    class DeactivateTests {

        @Test
        @DisplayName("Deactivates an active user")
        void deactivate_onActiveUser_succeeds() {
            assertTrue(user.isActive());

            user.deactivate();

            assertFalse(user.isActive());
        }

        @Test
        @DisplayName("Throws IllegalUserStateException on already inactive user")
        void deactivate_onInactiveUser_throwsException() {
            user.reconstituteActive(false);

            IllegalUserStateException ex =
                    assertThrows(IllegalUserStateException.class, () -> user.deactivate());
            assertTrue(ex.getMessage().contains("already inactive"));
        }
    }

    // ---------------------------------------------------------------------------
    //  Role Management: assignRole() / removeRole() / hasRole()
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("assignRole()")
    class AssignRoleTests {

        @Test
        @DisplayName("Assigns ADMIN role successfully (replaces existing roles)")
        void assignRole_admin_replacesExistingRoles() {
            user.assignRole(RoleType.USER);
            assertTrue(user.hasRole(RoleType.USER));

            user.assignRole(RoleType.ADMIN);

            assertTrue(user.hasRole(RoleType.ADMIN));
            assertFalse(user.hasRole(RoleType.USER));
            assertEquals(1, user.getRoles().size());
        }

        @Test
        @DisplayName("Assigns USER role successfully")
        void assignRole_user_succeeds() {
            user.assignRole(RoleType.USER);

            assertTrue(user.hasRole(RoleType.USER));
            assertEquals(1, user.getRoles().size());
        }

        @Test
        @DisplayName("Throws IllegalArgumentException when role is null")
        void assignRole_null_throwsException() {
            assertThrows(IllegalArgumentException.class, () -> user.assignRole(null));
        }
    }

    @Nested
    @DisplayName("removeRole()")
    class RemoveRoleTests {

        @Test
        @DisplayName("Throws IllegalStateException when removing the last role")
        void removeRole_lastRole_throwsException() {
            user.assignRole(RoleType.USER);
            assertEquals(1, user.getRoles().size());

            assertThrows(IllegalStateException.class, () -> user.removeRole(RoleType.USER));
        }

        @Test
        @DisplayName("Throws IllegalArgumentException when role is null")
        void removeRole_null_throwsException() {
            assertThrows(IllegalArgumentException.class, () -> user.removeRole(null));
        }

        @Test
        @DisplayName("No-op when removing a role the user doesn't have")
        void removeRole_notPresent_isNoOp() {
            user.assignRole(RoleType.USER);
            int sizeBefore = user.getRoles().size();

            user.removeRole(RoleType.ADMIN); // user only has USER

            assertEquals(sizeBefore, user.getRoles().size());
        }
    }

    @Nested
    @DisplayName("hasRole()")
    class HasRoleTests {

        @Test
        @DisplayName("Returns true when user has the role")
        void hasRole_present_returnsTrue() {
            user.assignRole(RoleType.ADMIN);
            assertTrue(user.hasRole(RoleType.ADMIN));
        }

        @Test
        @DisplayName("Returns false when user does not have the role")
        void hasRole_absent_returnsFalse() {
            user.assignRole(RoleType.USER);
            assertFalse(user.hasRole(RoleType.ADMIN));
        }
    }

    // ---------------------------------------------------------------------------
    //  getRoles() — unmodifiable view
    // ---------------------------------------------------------------------------

    @Test
    @DisplayName("getRoles() returns an unmodifiable set")
    void getRoles_returnsUnmodifiableSet() {
        user.assignRole(RoleType.USER);
        Set<RoleType> roles = user.getRoles();

        assertThrows(UnsupportedOperationException.class, () -> roles.add(RoleType.ADMIN));
        assertThrows(UnsupportedOperationException.class, () -> roles.remove(RoleType.USER));
    }

    // ---------------------------------------------------------------------------
    //  Profile Setters
    // ---------------------------------------------------------------------------

    @Test
    @DisplayName("Profile setters update fields correctly")
    void profileSetters_updateFields() {
        user.setDescription("A DDD enthusiast");
        user.setProfilePictureUrl("https://example.com/pic.jpg");
        user.setLinkedinUrl("https://linkedin.com/in/johndoe");
        user.setGithubUrl("https://github.com/johndoe");

        assertEquals("A DDD enthusiast", user.getDescription());
        assertEquals("https://example.com/pic.jpg", user.getProfilePictureUrl());
        assertEquals("https://linkedin.com/in/johndoe", user.getLinkedinUrl());
        assertEquals("https://github.com/johndoe", user.getGithubUrl());
    }

    // ---------------------------------------------------------------------------
    //  Infrastructure reconstitution
    // ---------------------------------------------------------------------------

    @Test
    @DisplayName("reconstituteActive() reconstitutes state without guards")
    void setActive_reconstitutesWithoutGuards() {
        user.reconstituteActive(false);
        assertFalse(user.isActive());

        user.reconstituteActive(true);
        assertTrue(user.isActive());
    }

    @Test
    @DisplayName("reconstituteRoles() reconstitutes roles without guards")
    void setRoles_reconstitutesWithoutGuards() {
        user.reconstituteRoles(Set.of(RoleType.ADMIN, RoleType.USER));

        assertTrue(user.hasRole(RoleType.ADMIN));
        assertTrue(user.hasRole(RoleType.USER));
        assertEquals(2, user.getRoles().size());
    }

    @Test
    @DisplayName("reconstituteRoles(null) defaults to empty set")
    void setRoles_null_defaultsToEmpty() {
        user.reconstituteRoles(null);
        assertNotNull(user.getRoles());
        assertTrue(user.getRoles().isEmpty());
    }

    // ---------------------------------------------------------------------------
    //  addRole — additive role management
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("AddRole Tests")
    class AddRoleTests {

        @Test
        @DisplayName("addRole() appends without clearing existing roles")
        void addRole_addsWithoutClearing() {
            user.assignRole(RoleType.USER);
            user.addRole(RoleType.ADMIN);

            assertTrue(user.hasRole(RoleType.USER));
            assertTrue(user.hasRole(RoleType.ADMIN));
            assertEquals(2, user.getRoles().size());
        }

        @Test
        @DisplayName("addRole() is idempotent for already-present roles")
        void addRole_existingRole_noChange() {
            user.assignRole(RoleType.USER);
            int sizeBefore = user.getRoles().size();
            user.addRole(RoleType.USER);

            assertEquals(sizeBefore, user.getRoles().size());
        }

        @Test
        @DisplayName("addRole(null) throws IllegalArgumentException")
        void addRole_null_throwsException() {
            assertThrows(IllegalArgumentException.class, () -> user.addRole(null));
        }
    }
}
