package com.example.filelogger

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.filelogger.databinding.ActivityMainBinding
import com.example.filelogger.logger.FileLogger
import java.util.Date

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var fileLogger: FileLogger

    private fun initFileLogger() {
        fileLogger = FileLogger(applicationContext).apply {
            initLogger(FileLogger.LOG_FILE_NAME, FileLogger.FILE_HEADER)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initFileLogger()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonFirst.setOnClickListener {
            captureLogs()
        }
    }

    private fun captureLogs() {
        fileLogger.writeLogsToFile(Date(), "c76c98a0c7f0", -41, 20, "1234567-1234-3214-6789")
    }
}