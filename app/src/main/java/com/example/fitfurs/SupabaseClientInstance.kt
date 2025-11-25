package com.example.fitfurs
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.storage.Storage  // Ensure this import is present
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseClientInstance {
    val client: SupabaseClient = createSupabaseClient(
        supabaseUrl = "https://vzbhcdkmzpehdabqadxq.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InZ6YmhjZGttenBlaGRhYnFhZHhxIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjM3MjA0MDMsImV4cCI6MjA3OTI5NjQwM30.cnQoglStV7vfru3KC1G6bOU2qcyVGzPFHW5pVgFzOEU"  // Replace with your actual anon key
    ) {
        // Only install Storage (no Auth needed)
        install(Storage)
        install(Postgrest)
    }
    val storage get() = client.storage  // This should now resolve
}