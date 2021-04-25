package com.phoenix.whatsappkhushiadmin

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.CannedAccessControlList
import com.github.hiteshsondhi88.libffmpeg.FFmpeg
import com.github.hiteshsondhi88.libffmpeg.FFmpegLoadBinaryResponseHandler
import com.phoenix.whatsappkhushiadmin.callbacks.callback
import com.phoenix.whatsappkhushiadmin.tool.TextOnVideo
import com.phoenix.whatsappkhushiadmin.utils.Utils
import kotlinx.android.synthetic.main.activity_upload.*
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*


class UploadActivity : AppCompatActivity() {

    private var outputPath = ""
    private var outputFileName = ""
    private var context: Context? = null

    val REQUEST_TAKE_GALLERY_VIDEO = 10
    var videoUri : Uri? = null
    var videoBitmap : Bitmap? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload)
        this.context = this

        if(intent.hasExtra("receivedVideo")){
            videoUri = intent.getParcelableExtra("receivedVideo")
            videoView.setVideoURI(videoUri)
            videoView.start()
            playButton.setImageResource(R.drawable.ic_pause)
        }
        else {
            playButton.isEnabled = false
            buttonUpload.isEnabled = false
        }
        textViewCategory.setText("Selected category: "+intent.getStringExtra("category"))
        videoView.setOnCompletionListener {
            videoView.seekTo(2000)
            playButton.setImageResource(R.drawable.ic_play)
        }
    }
    fun onClick(view: View){
        when(view.id){
            R.id.buttonSelect -> {
//                val intent = Intent()
//                intent.type = "video/*"
//                intent.action = Intent.ACTION_GET_CONTENT
//                startActivityForResult(intent,  REQUEST_TAKE_GALLERY_VIDEO)
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "video/*"
                startActivityForResult(intent, REQUEST_TAKE_GALLERY_VIDEO)
            }
            R.id.playButton -> {
                if(videoView.isPlaying){
                    videoView.pause()
                    playButton.setImageResource(R.drawable.ic_play)
                    Log.d("videoo","if")
                }
                else{
                    videoView.start()
                    playButton.setImageResource(R.drawable.ic_pause)
                    Log.d("videoo","else")
                }
            }
        }
    }
    fun upload(view: View) {

        val file = File(videoUri?.path) //create path from uri

        val split = file.path.split(":").toTypedArray() //split the path.

        val filePath = split[1] //assign it to a string(your choice).

        Log.e("tag",filePath)
        Log.e("tag",filePath)
        Log.e("tag",filePath)
        Log.e("tag",filePath)
        Log.e("tag",filePath)
        Log.e("tag",filePath)
        Log.e("tag",filePath)
        Log.e("tag",filePath)
        Log.e("tag",filePath)


        val ff = FFmpeg.getInstance(applicationContext)
        ff.loadBinary(object : FFmpegLoadBinaryResponseHandler {
            override fun onFinish() {
                Log.e("FFmpegLoad", "onFinish")
            }

            override fun onSuccess() {
                Log.e("FFmpegLoad", "onSuccess")
                TextOnVideo.with(context!!)
                    .setFile(filePath)
                    .setOutputPath(Utils.outputPath + "video")
                    .setOutputFileName("textOnVideo_" + System.currentTimeMillis() + ".mp4")
                    //.setFont(font) //Font .ttf of text
                    .setText("Text Displayed on Video!!") //Text to be displayed
                    .setColor("#50b90e") //Color of Text
                    .setSize("34") //Size of text
                    .addBorder(true) //This will add background with border on text
                    .setPosition(TextOnVideo.POSITION_CENTER_BOTTOM) //Can be selected
                   // .setCallback(this@UploadActivity)
                    .draw()
            }

            override fun onFailure() {
                Log.e("FFmpegLoad", "onFailure")
            }

            override fun onStart() {
            }
        })

        val progressBar = ProgressDialog(this);
        progressBar.setCancelable(true);
        progressBar.setMessage("Uploading Video...");
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBar.setProgress(0);
        progressBar.setMax(100);
        progressBar.show();

        val fileName = editTextName.text.trim()
        if(fileName.isEmpty()){
            return
        }
        val credentialsProvider =
            CognitoCachingCredentialsProvider(
                applicationContext,
                "ap-south-1:a523f97f-9e0a-4bcd-8955-d3a7e0cfa943",  // Identity pool ID
                Regions.AP_SOUTH_1 // Region
            )


        if(!copyFileFromUri(this,videoUri!!)) {
            Toast.makeText(applicationContext,"Failed",Toast.LENGTH_SHORT).show()
        }
//        else{
//            Toast.makeText(applicationContext,"success",Toast.LENGTH_SHORT).show()
//        }
        var f: File = File(filesDir.toString() + File.separator + "temp" + File.separator+ "akil.mp4")
//        var f: File = File(Environment.getExternalStorageDirectory().toString()+ "/akil.mp4")
        val s3: AmazonS3 = AmazonS3Client(credentialsProvider)
        val transferUtility = TransferUtility(s3, applicationContext)
        val observer = transferUtility.upload(
            "khushi-assets-admin",  //this is the bucket name on S3
            intent.getStringExtra("category")+"/" + fileName+"_" + getCurrTime() + "." + f.extension,  //this is the path and name
            f,  //path to the file locally
            CannedAccessControlList.PublicRead //to make the file public
        )
        observer.setTransferListener(object : TransferListener {
            override fun onStateChanged(id: Int, state: TransferState) {
                if (state.equals(TransferState.COMPLETED)) {
                    Toast.makeText(applicationContext,"Video Uploaded",Toast.LENGTH_SHORT).show()
                    progressBar.dismiss()
                    Log.d("awstest", "success")
                } else if (state.equals(TransferState.FAILED)) {
                    Toast.makeText(applicationContext,"Upload failed",Toast.LENGTH_SHORT).show()
                    progressBar.dismiss()
                    Log.d("awstest", "failed")
                }
            }
            override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {
                val percentage = (bytesCurrent / bytesTotal * 100).toInt()

            }
            override fun onError(id: Int, ex: Exception) {
                Log.d("awstest", ex.toString())
            }
        })
    }
    fun getCurrTime():String{
        var c: Date = Calendar.getInstance().getTime();
        var df : SimpleDateFormat = SimpleDateFormat("ddMMyyyyHHmmss");
        val formattedDate = df.format(c)
        Log.d("awstest",formattedDate)
        return formattedDate
    }
//    fun getRealPathFromURI(
//        context: Context,
//        contentUri: Uri?
//    ): String? {
//        var cursor: Cursor? = null
//        return try {
//            val proj =
//                arrayOf(MediaStore.Images.Media.DATA)
//            cursor = context.contentResolver.query(contentUri!!, proj, null, null, null)
//            val column_index = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
//            cursor.moveToFirst()
//            cursor.getString(column_index)
//        } finally {
//            cursor?.close()
//        }
//    }
//    fun getPath(uri: Uri?): String? {
//        // just some safety built in
//        if (uri == null) {
//            // TODO perform some logging or show user feedback
//            return null
//        }
//        // try to retrieve the image from the media store first
//        // this will only work for images selected from gallery
//        val projection =
//            arrayOf(MediaStore.Images.Media.DATA)
//        val cursor = managedQuery(uri, projection, null, null, null)
//        if (cursor != null) {
//            val column_index = cursor
//                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
//            cursor.moveToFirst()
//            return cursor.getString(column_index)
//        }
//        // this is our fallback here
//        return uri.path
//    }
//    private fun getFilePathForN(
//        uri: Uri,
//        context: Context
//    ): String? {
//        val returnCursor =
//            context.contentResolver.query(uri, null, null, null, null)
//        /*
//     * Get the column indexes of the data in the Cursor,
//     *     * move to the first row in the Cursor, get the data,
//     *     * and display it.
//     * */
//        val nameIndex = returnCursor!!.getColumnIndex(OpenableColumns.DISPLAY_NAME)
//        val sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE)
//        returnCursor.moveToFirst()
//        val name = returnCursor.getString(nameIndex)
//        val size = java.lang.Long.toString(returnCursor.getLong(sizeIndex))
//        val file = File(context.filesDir, name)
//        try {
//            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
//            val outputStream = FileOutputStream(file)
//            var read = 0
//            val maxBufferSize = 1 * 1024 * 1024
//            val bytesAvailable: Int = inputStream!!.available()
//
//            //int bufferSize = 1024;
//            val bufferSize = Math.min(bytesAvailable, maxBufferSize)
//            val buffers = ByteArray(bufferSize)
//            while (inputStream.read(buffers).also({ read = it }) != -1) {
//                outputStream.write(buffers, 0, read)
//            }
//            Log.e("File Size", "Size " + file.length())
//            inputStream.close()
//            outputStream.close()
//            Log.e("File Path", "Path " + file.path)
//            Log.e("File Size", "Size " + file.length())
//        } catch (e: java.lang.Exception) {
//            Log.e("Exception", e.message)
//        }
//        return file.path
//    }


    fun copyFileFromUri(
        context: Context,
        fileUri: Uri
    ): Boolean {
        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null
        try {
            val content: ContentResolver = context.contentResolver
            inputStream = content.openInputStream(fileUri)
            val root: File = Environment.getExternalStorageDirectory()
            if (root == null) {
//                Log.d(FragmentActivity.TAG, "Failed to get root")
            }

            // create a directory
            val saveDirectory = File(filesDir
                    .toString() + File.separator + "temp" + File.separator
            )
            // create direcotory if it doesn't exists
            saveDirectory.mkdirs()
            outputStream = FileOutputStream(filesDir.toString() + File.separator + "temp" + File.separator+ "akil.mp4") // filename.png, .mp3, .mp4 ...
//            outputStream = FileOutputStream(root.toString()+ "/akil.mp4") // filename.png, .mp3, .mp4 ..
            if (outputStream != null) {
//                Log.e(FragmentActivity.TAG, "Output Stream Opened successfully")
            }
            val buffer = ByteArray(1000)
            var bytesRead = 0
            while (inputStream!!.read(buffer, 0, buffer.size).also { bytesRead = it } >= 0) {
                outputStream.write(buffer, 0, buffer.size)
            }
        } catch (e: java.lang.Exception) {
            return false
//            Log.e(FragmentActivity.TAG, "Exception occurred " + e.message)
        } finally {
            inputStream!!.close()
            outputStream!!.close()
        }
        return true
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_TAKE_GALLERY_VIDEO) {
                val selectedVideoUri: Uri? = data!!.data
                videoUri = selectedVideoUri
                videoView.setVideoURI(selectedVideoUri)
                videoView.start()
                playButton.isEnabled = true
                playButton.setImageResource(R.drawable.ic_pause)
                buttonUpload.isEnabled = true
            }
        }
    }


}
