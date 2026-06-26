package com.example.data.network

import com.example.data.ClientDeal
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

object NetworkParser {
    val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    fun parseJobsList(json: String): List<ClientDeal> {
        // Option 1: Wrapped in Laravel Resource layout like { "data": [ ... ] }
        try {
            val adapter = moshi.adapter(JobResponseWrapper::class.java)
            val wrapper = adapter.fromJson(json)
            if (wrapper?.data != null) {
                return wrapper.data.map { it.toClientDeal() }
            }
        } catch (e: Exception) {
            // Squelch and try direct list
        }

        // Option 2: Direct JSON list [ { ... }, { ... } ]
        try {
            val listType = com.squareup.moshi.Types.newParameterizedType(List::class.java, NetworkJob::class.java)
            val adapter = moshi.adapter<List<NetworkJob>>(listType)
            val list = adapter.fromJson(json)
            if (list != null) {
                return list.map { it.toClientDeal() }
            }
        } catch (e: Exception) {
            // Squelch
        }

        return emptyList()
    }

    fun parseJobDetail(json: String): ClientDeal? {
        // Option 1: Wrapped in Laravel Resource layout like { "data": { ... } }
        try {
            val adapter = moshi.adapter(JobDetailResponseWrapper::class.java)
            val wrapper = adapter.fromJson(json)
            if (wrapper?.data != null) {
                return wrapper.data.toClientDeal()
            }
        } catch (e: Exception) {
            // Squelch and try direct object
        }

        // Option 2: Direct JSON object
        try {
            val adapter = moshi.adapter(NetworkJob::class.java)
            val job = adapter.fromJson(json)
            if (job != null) {
                return job.toClientDeal()
            }
        } catch (e: Exception) {
            // Squelch
        }

        return null
    }

    fun NetworkJob.toClientDeal(): ClientDeal {
        // Маппинг статусов с API (lowercase) → локальные enum
        val resolvedLocalStatus = when (status?.lowercase()) {
            "new"              -> "NEW"
            "diag"             -> "DIAGNOSTICS"
            "in_progress"      -> "IN_PROGRESS"
            "waiting_decision" -> "AWAITING_DECISION"
            "waiting_parts"    -> "AWAITING_PARTS"
            "done"             -> "READY"
            "delivered"        -> "ISSUED"
            "canceled"         -> "CANCELLED"
            else               -> "NEW"
        }

        return ClientDeal(
            id = id ?: 0L,
            clientName = client_name ?: clientName ?: "Без имени",
            phone = phone ?: "",
            email = email ?: "",
            company = company ?: "",
            dealValue = deal_value ?: dealValue ?: value ?: amount ?: 0.0,
            status = resolvedLocalStatus,
            detailedStatus = resolvedLocalStatus,
            complaint = complaint ?: "",
            diagnostics = diagnostics ?: "",
            solution = solution ?: "",
            visitDate = visit_date ?: visitDate ?: "",
            remindDate = remind_date ?: remindDate ?: "",
            laborMinutes = labor_minutes ?: laborMinutes ?: 0,
            myShare = my_share ?: myShare ?: 50,
            alexShare = alex_share ?: alexShare ?: 50,
            carMake = car_make ?: carMake ?: "",
            carModel = car_model ?: carModel ?: "",
            carLicense = car_license ?: carLicense ?: "",
            carVin = car_vin ?: carVin ?: "",
            carYear = car_year ?: carYear ?: "",
            carEngine = car_engine ?: carEngine ?: "",
            timestamp = created_at?.let {
                try {
                    java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                        .parse(it)?.time
                } catch (e: Exception) { null }
            } ?: timestamp ?: System.currentTimeMillis()
        )
    }

    fun fromClientDeal(deal: ClientDeal): NetworkJob {
        val serverStatus = when (deal.status) {
            "NEW" -> "new"
            "DIAGNOSTICS" -> "diag"
            "IN_PROGRESS" -> "in_progress"
            "AWAITING_DECISION" -> "waiting_decision"
            "AWAITING_PARTS" -> "waiting_parts"
            "READY" -> "done"
            "ISSUED" -> "delivered"
            "CANCELLED" -> "canceled"
            else -> "new"
        }

        val resolvedDetailedStatus = when (deal.status) {
            "NEW" -> "new"
            "DIAGNOSTICS" -> "diag"
            "IN_PROGRESS" -> "in_progress"
            "AWAITING_DECISION" -> "waiting_decision"
            "AWAITING_PARTS" -> "waiting_parts"
            "READY" -> "done"
            "ISSUED" -> "delivered"
            "CANCELLED" -> "canceled"
            else -> "new"
        }

        return NetworkJob(
            id = if (deal.id == 0L) null else deal.id,
            client_name = deal.clientName,
            clientName = deal.clientName,
            phone = deal.phone,
            email = deal.email,
            company = deal.company,
            deal_value = deal.dealValue,
            dealValue = deal.dealValue,
            value = deal.dealValue,
            amount = deal.dealValue,
            status = serverStatus,
            detailed_status = resolvedDetailedStatus,
            detailedStatus = resolvedDetailedStatus,
            complaint = deal.complaint,
            diagnostics = deal.diagnostics,
            solution = deal.solution,
            visit_date = deal.visitDate,
            visitDate = deal.visitDate,
            remind_date = deal.remindDate,
            remindDate = deal.remindDate,
            labor_minutes = deal.laborMinutes,
            laborMinutes = deal.laborMinutes,
            my_share = deal.myShare,
            myShare = deal.myShare,
            alex_share = deal.alexShare,
            alexShare = deal.alexShare,
            car_make = deal.carMake,
            carMake = deal.carMake,
            car_model = deal.carModel,
            carModel = deal.carModel,
            car_license = deal.carLicense,
            carLicense = deal.carLicense,
            car_vin = deal.carVin,
            carVin = deal.carVin,
            car_year = deal.carYear,
            carYear = deal.carYear,
            car_engine = deal.carEngine,
            carEngine = deal.carEngine,
            timestamp = deal.timestamp
        )
    }
}
