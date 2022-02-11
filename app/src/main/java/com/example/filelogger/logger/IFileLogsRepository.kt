package com.example.filelogger.logger

interface IFileLogsRepository {
    fun initLogger(fileName: String, headers: String)
    fun writeLogsToFile(tag: String, message: String)
    fun writeLogsToFile(vararg args: Any)
}