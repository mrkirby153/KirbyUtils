package me.mrkirby153.kcutils.spring.coroutine.config

import me.mrkirby153.kcutils.spring.coroutine.CoroutineTransactionHandler
import mu.KotlinLogging
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.transaction.support.TransactionTemplate

/**
 * Enables transactional coroutines
 */
@Target(AnnotationTarget.CLASS)
@Import(CoroutineTransactionConfiguration::class)
annotation class EnableTransactionalCoroutines

private val log = KotlinLogging.logger { }

/**
 * Automatic configuration for coroutine transactions
 */
@Configuration
open class CoroutineTransactionConfiguration(
    private val template: TransactionTemplate
) {

    init {
        log.info { "Automatically setting default coroutine transaction handler based on spring context" }
        CoroutineTransactionHandler.setDefault(template)
    }

    @Bean
    open fun coroutineTransactionHandler() = CoroutineTransactionHandler(template)
}