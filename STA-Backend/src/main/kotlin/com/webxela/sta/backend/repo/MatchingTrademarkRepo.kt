package com.webxela.sta.backend.repo

import com.webxela.sta.backend.domain.model.MatchingTrademark
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class MatchingTrademarkRepo {

    private val logger = LoggerFactory.getLogger(MatchingTrademarkRepo::class.java)

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    @Transactional
    fun createTableIfNotExists(tableName: String) {
        try {
            if (!tableExists(tableName)) {
                val query = """
                    CREATE TABLE IF NOT EXISTS $tableName (
                        match_result_id SERIAL PRIMARY KEY,
                        journal_app_number VARCHAR(255) NOT NULL,
                        our_trademark_app_numbers TEXT NOT NULL,
                        tm_class VARCHAR(255) NOT NULL,
                        journal_number VARCHAR(255) NOT NULL
                    );
                """.trimIndent()
                entityManager.createNativeQuery(query).executeUpdate()
            } else {
                logger.info("Table $tableName already exists, skipping creation.")
            }
        } catch (ex: Exception) {
            logger.error("Error creating table $tableName", ex)
            throw ex
        }
    }

    @Transactional
    fun addAll(tableName: String, matchingTrademarks: List<MatchingTrademark>) {
        try {
            createTableIfNotExists(tableName)
            val baseQuery =
                "INSERT INTO $tableName (journal_app_number, our_trademark_app_numbers, tm_class, journal_number) VALUES (:journal_app_number, :our_trademark_app_numbers, :tm_class, :journal_number)"

            matchingTrademarks.forEach { trademark ->
                val query = entityManager.createNativeQuery(baseQuery)
                    .setParameter("journal_app_number", trademark.journalAppNumber)
                    .setParameter("our_trademark_app_numbers", trademark.ourTrademarkAppNumbers.joinToString(","))
                    .setParameter("tm_class", trademark.tmClass)
                    .setParameter("journal_number", trademark.journalNumber)
                query.executeUpdate()
            }
        } catch (ex: Exception) {
            logger.error("Error while inserting data into dynamic database", ex)
            throw ex
        }
    }


    @Transactional
    fun findAll(tableName: String): List<MatchingTrademark> {
        try {
            if (!tableExists(tableName)) {
                logger.info("Table $tableName does not exist.")
                return emptyList() // Or handle it as per your logic.
            }
            val query =
                entityManager.createNativeQuery("SELECT match_result_id, journal_app_number, our_trademark_app_numbers, tm_class, journal_number FROM $tableName")
            val results = query.resultList
            return results.map {
                val row = it as Array<*>
                MatchingTrademark(
                    matchResultId = (row[0] as Number).toLong(),
                    journalAppNumber = row[1] as String,
                    ourTrademarkAppNumbers = (row[2] as String).split(","),
                    tmClass = row[3] as String,
                    journalNumber = row[4] as String
                )
            }
        } catch (ex: Exception) {
            logger.error("Error while fetching data from dynamic table $tableName", ex)
            throw ex
        }
    }

    @Transactional
    fun deleteAll(tableName: String) {
        try {
            if (!tableExists(tableName)) {
                logger.info("Table $tableName does not exist, nothing to delete.")
                return
            }
            val query = entityManager.createNativeQuery("DELETE FROM $tableName")
            query.executeUpdate()
        } catch (ex: Exception) {
            logger.error("Error while deleting data from dynamic table $tableName", ex)
            throw ex
        }
    }

    @Transactional
    fun tableExists(tableName: String): Boolean {
        return try {
            val query = entityManager.createNativeQuery(
                """
            SELECT EXISTS (
                SELECT 1 
                FROM information_schema.tables 
                WHERE table_schema = 'public' AND table_name = :tableName
            )
            """.trimIndent()
            )
            query.setParameter("tableName", tableName.lowercase())
            val result = query.singleResult as Boolean
            result
        } catch (ex: Exception) {
            logger.error("Error while checking table existence for $tableName", ex)
            false
        }
    }

    @Transactional
    fun replaceAll(tableName: String, matchingTrademarks: List<MatchingTrademark>) {
        try {
            createTableIfNotExists(tableName)

            // First, delete all the existing records in the table
            deleteAll(tableName)

            // Then, insert the new records
            val baseQuery =
                "INSERT INTO $tableName (journal_app_number, our_trademark_app_numbers, tm_class, journal_number) VALUES (:journal_app_number, :our_trademark_app_numbers, :tm_class, :journal_number)"

            matchingTrademarks.forEach { trademark ->
                val query = entityManager.createNativeQuery(baseQuery)
                    .setParameter("journal_app_number", trademark.journalAppNumber)
                    .setParameter("our_trademark_app_numbers", trademark.ourTrademarkAppNumbers.joinToString(","))
                    .setParameter("tm_class", trademark.tmClass)
                    .setParameter("journal_number", trademark.journalNumber)
                query.executeUpdate()
            }
        } catch (ex: Exception) {
            logger.error("Error while replacing data in table $tableName", ex)
            throw ex
        }
    }

}
