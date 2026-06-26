package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.data.CrmDatabase
import com.example.data.CrmRepository
import com.example.data.network.AuthManager
import com.example.data.network.AuthInterceptor
import com.example.data.network.ApiService
import com.example.data.network.NetworkParser
import com.example.ui.CrmDashboard
import com.example.ui.CrmViewModel
import com.example.ui.CrmViewModelFactory
import com.example.ui.theme.MyApplicationTheme
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    
    // Initialize Local SQLite Database & Repository
    val database = CrmDatabase.getDatabase(this)
    val repository = CrmRepository(database.crmDao())
    
    // Initialize Network services
    val authManager = AuthManager(applicationContext)
    
    val loggingInterceptor = HttpLoggingInterceptor().apply {
      level = HttpLoggingInterceptor.Level.BODY
    }
    
    val okHttpClient = OkHttpClient.Builder()
      .addInterceptor(AuthInterceptor(authManager))
      .addInterceptor(loggingInterceptor)
      .build()
      
    val retrofit = Retrofit.Builder()
      .baseUrl("http://194.180.207.24/")
      .client(okHttpClient)
      .addConverterFactory(MoshiConverterFactory.create(NetworkParser.moshi))
      .build()
      
    val apiService = retrofit.create(ApiService::class.java)
    
    // Instantiate CrmViewModel with repository, authManager, and apiService injection
    val viewModel: CrmViewModel by viewModels {
      CrmViewModelFactory(repository, authManager, apiService)
    }

    setContent {
      MyApplicationTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          CrmDashboard(viewModel = viewModel)
        }
      }
    }
  }
}
