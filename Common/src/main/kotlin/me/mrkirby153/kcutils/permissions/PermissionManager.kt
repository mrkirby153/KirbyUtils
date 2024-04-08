package me.mrkirby153.kcutils.permissions


/**
 * A generic permission manager for granting and denying arbitrary permissions.
 *
 * ## Roles
 *
 * These are the core primitive of the permission system, [Permission]s can be attached to [Role]s
 * and granted/denied. A role is registered by calling [add], specifying the role and an optional
 * list of parents for this role. Each role will inherit all inheritable (defined when granting/
 * denying) permissions from the list of parents.
 *
 * For example, if `ROLE_1` is defined as a parent of `ROLE_2`, all inheritable permissions granted
 * to `ROLE_1` will be transitively granted to `ROLE_2`.
 *
 * ## Permissions
 *
 * These represent the actual permissions that can be granted or denied by calling [grant],
 * [revoke], or [deny].
 *
 * Permissions can be checked by calling [check], which will evaluate the permission graph against
 * the defined roles and the result returned.
 */
class PermissionManager<T : Role> {
    private val roleNodes = mutableMapOf<T, Node<T>>()

    /**
     * Adds a [role] to the permission manager with optional [parents]. A role will inherit
     * all inheritable permissions from the parents.
     */
    fun add(role: T, vararg parents: T) {
        roleNodes[role] = Node(role, *parents)
    }

    /**
     * Removes a [role] from the permission manager
     */
    fun remove(role: T) {
        roleNodes.remove(role)
    }

    /**
     * Grants the given [permission] to the [role]. If [inherit] is true, this permission will
     * also be granted to all children of this role.
     */
    fun grant(role: T, permission: Permission, inherit: Boolean = true) {
        val roleNode = roleNodes[role]
        requireNotNull(roleNode) { "Role not registered" }
        roleNode.grant(permission, inherit)
    }

    /**
     * Revokes the given [permission] from the [role].
     */
    fun revoke(role: T, permission: Permission) {
        val roleNode = roleNodes[role]
        requireNotNull(roleNode) { "Role not registered" }
        roleNode.revoke(permission)
    }

    /**
     * Denies the given [permission] to the [role]. If [inherit] is true, this permission will also
     * be denied to all children of this role
     */
    fun deny(role: T, permission: Permission, inherit: Boolean = false) {
        val roleNode = roleNodes[role]
        requireNotNull(roleNode) { "Role not registered" }
        roleNode.deny(permission, inherit)
    }


    /**
     * Checks if the given [role] has the required [permission]. If no explicit grants are defined,
     * [default] is returned.
     */
    fun check(role: T, permission: Permission, default: Boolean = false): Boolean {
        val toConsider = parents(role)
        val results = toConsider.map {
            val node = roleNodes[it]
            requireNotNull(node) { "Role not registered" }
            Pair(node.granted(permission, it != role), node.denied(permission, it != role))
        }
        val result = (results.any { it.first } && !results.any { it.second }) || default
        return result
    }


    private fun parents(role: T, seen: Set<T> = emptySet()): Set<T> {
        val node = roleNodes[role] ?: return emptySet()
        if (node.parents.isEmpty()) return setOf(role)
        val result = mutableSetOf(role)
        node.parents.forEach {
            // Prevent infinite loops
            if (!seen.contains(it)) {
                result.addAll(parents(it, result))
            }
        }
        return result
    }
}


/**
 * Marker interface indicating that this class is a role. These are usually applied to enums, but
 * not exclusively.
 */
interface Role

/**
 * Marker interface indicating that this class is a permission. This is applied to enums
 */
interface Permission


internal class Node<T>(vararg val parents: T) {

    private data class GrantedPermission(val permission: Permission, val inherit: Boolean = false)

    private val grantedPermissions = mutableSetOf<GrantedPermission>()
    private val deniedPermissions = mutableSetOf<GrantedPermission>()

    fun grant(permission: Permission, inherit: Boolean) {
        grantedPermissions.add(GrantedPermission(permission, inherit))
    }

    fun revoke(permission: Permission) {
        grantedPermissions.removeIf { it.permission == permission }
    }

    fun deny(permission: Permission, inherit: Boolean) {
        deniedPermissions.add(GrantedPermission(permission, inherit))
    }


    fun granted(permission: Permission, inherited: Boolean): Boolean =
        calculate(permission, inherited, grantedPermissions)

    fun denied(permission: Permission, inherited: Boolean): Boolean =
        calculate(permission, inherited, deniedPermissions)

    private fun calculate(
        permission: Permission,
        inherited: Boolean,
        list: Set<GrantedPermission>
    ): Boolean {
        val perm = list.find { it.permission == permission }
        return if (inherited) {
            perm != null && perm.inherit
        } else {
            perm != null
        }
    }
}