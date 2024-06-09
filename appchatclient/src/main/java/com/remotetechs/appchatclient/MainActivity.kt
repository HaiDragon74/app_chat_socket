package com.remotetechs.appchatclient
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.remotetechs.appchatclient.databinding.ActivityMainBinding
import com.remotetechs.appchatclient.model.Message
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    companion object{
        const val KEY_MESSAGE="key_message"
    }
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}