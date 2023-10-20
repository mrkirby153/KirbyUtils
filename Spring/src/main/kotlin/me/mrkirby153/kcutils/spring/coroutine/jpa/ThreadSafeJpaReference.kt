package me.mrkirby153.kcutils.spring.coroutine.jpa

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.findByIdOrNull

/**
 * A JPA reference that is safe to pass between threads.
 *
 * This functions by storing the entity's primary key and retrieving it on-demand when [get] is called.
 * This allows for safe passage of the object between threads
 */
class ThreadSafeJpaReference<Obj, Id>(
    private val repo: JpaRepository<Obj, Id>,
    private val id: Id
) {
    private val cachedInstance: ThreadLocal<Obj?> = ThreadLocal()
    private val cacheLoaded: ThreadLocal<Boolean> = ThreadLocal.withInitial { false }

    /**
     * Retrieves an instance of the JPA entity for the current thread. The object returned by this
     * function is cached.
     */
    fun get(): Obj? {
        return if (cacheLoaded.get()) {
            cachedInstance.get()
        } else {
            val refreshed = repo.findByIdOrNull(id)
            cachedInstance.set(refreshed)
            cacheLoaded.set(true)
            refreshed
        }
    }

    /**
     * Force a refresh on the next invocation of [get]
     */
    fun refresh() {
        cacheLoaded.set(false)
    }
}

/**
 * Converts an [entityId] into a [ThreadSafeJpaReference]
 */
fun <Obj, Id> JpaRepository<Obj, Id>.toThreadSafeReference(entityId: Id) =
    ThreadSafeJpaReference(this, entityId)