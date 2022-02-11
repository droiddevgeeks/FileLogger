package com.example.filelogger.logger

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

class FileLogger(private val context: Context) : IFileLogsRepository {

    companion object {
        const val LOG_FILE_NAME: String = "RSSILogs"
        const val FILE_HEADER: String =
            "TimeStamp,Peripheral Name,RSSI,TX Power,Advertisement UUID\n"

        private const val FOLDER_PATH = "/LOGS/"
        private val TAG = FileLogger::class.java.simpleName
    }

    private var fileUri: Uri? = null

    override fun initLogger(fileName: String, headers: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            handleAboveAndroid10FileCreation(fileName, headers)
        } else {
            handleBelowAndroid10FileCreation(fileName, headers)
        }
    }

    private fun handleAboveAndroid10FileCreation(fileName: String, headers: String) {
        findAndInitExistingFileUri(fileName)
        if (fileUri == null) initNewFileUri(fileName, headers)
    }

    private fun handleBelowAndroid10FileCreation(fileName: String, headers: String) {
        val rootDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            FOLDER_PATH
        )
        if (rootDir.exists().not()) rootDir.mkdirs()
        val file = File(rootDir, "${fileName}.csv")
        if (file.exists().not()) {
            file.createNewFile()
            fileUri = Uri.fromFile(file)
            writeLog(headers)
        } else {
            fileUri = Uri.fromFile(file)
        }
    }

    @SuppressLint("Range")
    private fun findAndInitExistingFileUri(fileName: String) {
        val uri = MediaStore.Files.getContentUri("external")
        val cursor = getCursor(uri)
        cursor?.let {
            while (cursor.moveToNext()) {
                val existingFileName =
                    cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME))
                if (existingFileName.equals("$fileName.csv")) {
                    val id = cursor.getLong(cursor.getColumnIndex(MediaStore.MediaColumns._ID))
                    fileUri = ContentUris.withAppendedId(uri, id)
                    break
                }
            }
            cursor.close()
        }
    }

    private fun getCursor(uri: Uri): Cursor? {
        val selection = MediaStore.MediaColumns.RELATIVE_PATH + "=?"
        val selectionArgs = arrayOf(Environment.DIRECTORY_DOWNLOADS + FOLDER_PATH)
        return context.contentResolver.query(uri, null, selection, selectionArgs, null)
    }

    private fun initNewFileUri(fileName: String, headers: String) {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
            put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                Environment.DIRECTORY_DOWNLOADS + FOLDER_PATH
            )
        }
        fileUri = context.contentResolver.insert(
            MediaStore.Files.getContentUri("external"),
            contentValues
        )
        writeLog(headers)
    }

    override fun writeLogsToFile(tag: String, message: String) {
        Log.d(tag, message)
        writeLog("$tag,$message\n")
    }

    override fun writeLogsToFile(vararg args: Any) {
        Log.d(TAG, args.joinToString())
        val input = args.joinToString()
        writeLog(input + "\n")
    }

    private fun writeLog(message: String) {
        try {
            val file = fileUri?.let { context.contentResolver.openFileDescriptor(it, "wa") }
            file?.let {
                val fileOutputStream = FileOutputStream(it.fileDescriptor)
                fileOutputStream.write(message.toByteArray())
                fileOutputStream.close()
                it.close()
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}