package com.webxela.sta.backend.repo

import com.webxela.sta.backend.domain.model.Trademark
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class DynamicJournalTmRepo {

    private val logger = LoggerFactory.getLogger(DynamicJournalTmRepo::class.java)

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    @Transactional
    fun createTableIfNotExists(tableName: String) {
        try {
            val query = """
            CREATE TABLE IF NOT EXISTS $tableName (
                tm_id BIGSERIAL PRIMARY KEY,
                status VARCHAR(255),
                application_number VARCHAR(255) NOT NULL,
                tm_class VARCHAR(255) NOT NULL,
                date_of_application VARCHAR(255),
                appropriate_office Text,
                state VARCHAR(255),
                country VARCHAR(255),
                filing_mode VARCHAR(255),
                tm_applied_for TEXT NOT NULL,
                tm_category Text,
                tm_type TEXT,
                user_details TEXT,
                cert_detail TEXT,
                valid_up_to TEXT,
                proprietor_name TEXT,
                proprietor_address TEXT,
                email_id TEXT,
                agent_name TEXT,
                agent_address TEXT,
                publication_details TEXT
            );
        """.trimIndent()
            entityManager.createNativeQuery(query).executeUpdate()
        } catch (ex: Exception) {
            logger.error("Error creating table $tableName", ex)
            throw ex
        }
    }

    @Transactional
    fun addAll(tableName: String, trademarks: List<Trademark>) {
        try {
            createTableIfNotExists(tableName)
            val baseQuery =
                "INSERT INTO $tableName (status, application_number, tm_class, date_of_application, appropriate_office, state, country, filing_mode, tm_applied_for, tm_category, tm_type, user_details, cert_detail, valid_up_to, proprietor_name, proprietor_address, email_id, agent_name, agent_address, publication_details) VALUES (:status, :application_number, :tm_class, :date_of_application, :appropriate_office, :state, :country, :filing_mode, :tm_applied_for, :tm_category, :tm_type, :user_details, :cert_detail, :valid_up_to, :proprietor_name, :proprietor_address, :email_id, :agent_name, :agent_address, :publication_details)"

            trademarks.forEach { trademark ->
                val query = entityManager.createNativeQuery(baseQuery)
                    .setParameter("status", trademark.status)
                    .setParameter("application_number", trademark.applicationNumber)
                    .setParameter("tm_class", trademark.tmClass)
                    .setParameter("date_of_application", trademark.dateOfApplication)
                    .setParameter("appropriate_office", trademark.appropriateOffice)
                    .setParameter("state", trademark.state)
                    .setParameter("country", trademark.country)
                    .setParameter("filing_mode", trademark.filingMode)
                    .setParameter("tm_applied_for", trademark.tmAppliedFor)
                    .setParameter("tm_category", trademark.tmCategory)
                    .setParameter("tm_type", trademark.tmType)
                    .setParameter("user_details", trademark.userDetails)
                    .setParameter("cert_detail", trademark.certDetail)
                    .setParameter("valid_up_to", trademark.validUpTo)
                    .setParameter("proprietor_name", trademark.proprietorName)
                    .setParameter("proprietor_address", trademark.proprietorAddress)
                    .setParameter("email_id", trademark.emailId)
                    .setParameter("agent_name", trademark.agentName)
                    .setParameter("agent_address", trademark.agentAddress)
                    .setParameter("publication_details", trademark.publicationDetails)

                query.executeUpdate()
            }

        } catch (ex: Exception) {
            logger.error("Error while inserting data to dynamic database", ex)
            throw ex
        }
    }

    @Transactional
    fun findAll(tableName: String): List<Trademark> {
        return try {
            val query = """
            SELECT * FROM $tableName
            """.trimIndent()

            val resultList = entityManager.createNativeQuery(query, Trademark::class.java).resultList
            resultList as List<Trademark>
        } catch (ex: Exception) {
            logger.error("Error fetching records from $tableName", ex)
            throw ex
        }
    }

    @Transactional
    fun findByApplicationNumber(journalNumber: String, applicationNumber: String): Trademark? {
        val tableName = "journal_$journalNumber"
        return try {
            val query = """
            SELECT * FROM $tableName WHERE application_number = :applicationNumber
            """.trimIndent()

            entityManager.createNativeQuery(query, Trademark::class.java)
                .setParameter("applicationNumber", applicationNumber)
                .singleResult as? Trademark
        } catch (ex: Exception) {
            logger.error("Error fetching record by application number $applicationNumber from $tableName", ex)
            null
        }
    }

    @Transactional
    fun deleteByApplicationNumber(tableName: String, applicationNumber: String) {
        try {
            val query = """
            DELETE FROM $tableName WHERE application_number = :applicationNumber
            """.trimIndent()

            entityManager.createNativeQuery(query)
                .setParameter("applicationNumber", applicationNumber)
                .executeUpdate()
        } catch (ex: Exception) {
            logger.error("Error deleting record from $tableName", ex)
            throw ex
        }
    }

    @Transactional
    fun findInAllTmEverywhere(applicationNumber: String): Trademark? {
        try {
            val tableNamesQuery = """
            SELECT table_name 
            FROM information_schema.tables 
            WHERE table_name LIKE 'journal_%'
        """.trimIndent()

            val tableNames = entityManager.createNativeQuery(tableNamesQuery).resultList as List<String>

            for (tableName in tableNames) {
                try {
                    val query = """
                    SELECT * FROM $tableName WHERE application_number = :applicationNumber
                """.trimIndent()

                    val result = entityManager.createNativeQuery(query, Trademark::class.java)
                        .setParameter("applicationNumber", applicationNumber)
                        .singleResult as? Trademark

                    if (result != null) {
                        logger.info("Record found in table $tableName")
                        return result
                    }
                } catch (ex: Exception) {
                    logger.error("Error fetching record from $tableName", ex)
                }
            }
            logger.info("No record found for application number $applicationNumber in any journal tables.")
            return null

        } catch (ex: Exception) {
            logger.error("Error finding application number $applicationNumber in all journal tables", ex)
            throw ex
        }
    }

}
