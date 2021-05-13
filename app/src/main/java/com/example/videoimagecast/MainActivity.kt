package com.example.videoimagecast

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.media.MediaPlayer
import android.media.MediaPlayer.OnPreparedListener
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.View.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.webkit.WebView
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import com.example.videoimagecast.jsonDataClass.idDataClass
import com.example.videoimagecast.jsonDataClass.jsonDataClass
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import java.io.*
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import kotlin.collections.ArrayList
import kotlin.random.Random


class MainActivity : AppCompatActivity() {


    var jsonFileName = "file.json"//////////////////////////////////////////////////////////////////////////////////////////////

    var activeScreenId = "1";
    var screenIdList = ArrayList<String>()
    var screenIdListIterator:Int = 0
    var noOfScreen:Int = 0


    lateinit var  gsonData: jsonDataClass
    private lateinit var path: File
    lateinit var jsonPath : File
    lateinit var archivePath : File
    lateinit var downloadPath : File
    lateinit var currentFilesPath : File
    lateinit var currentFiles1Path : File
    lateinit var zipFilePath:File


    private var x1: Float = 0f
    private var x2: Float = 0f
    private val MIN_DISTANCE_LR = 150

    private lateinit  var screenTimer: CountDownTimer

    lateinit var checkDownloadTimer:CountDownTimer
    lateinit var heartBeatTimer:CountDownTimer


    


    lateinit var slideImageTimer1: CountDownTimer////////////////////////////////////////////////////////
    lateinit var slideImageTimer2: CountDownTimer
    lateinit var slideImageTimer3: CountDownTimer
    lateinit var slideImageTimer4: CountDownTimer
    lateinit var slideImageTimer5: CountDownTimer
    lateinit var slideImageTimer6: CountDownTimer
    lateinit var slideImageTimer7: CountDownTimer
    lateinit var slideImageTimer8: CountDownTimer
    lateinit var slideImageTimer9: CountDownTimer
    lateinit var slideImageTimer10: CountDownTimer


    var downloadSuccess:Boolean = false
   // var downloadUrl:String?=null


    var zipFileName:String = "CurrentFiles.zip"
    //var zipExtractSuccess:Boolean = false


    var  possibleColorStrings: ArrayList<String> = ArrayList<String>(listOf("red", "blue", "green", "black", "white",
            "gray", "cyan", "magenta", "yellow", "lightgray", "darkgray", "grey",
            "lightgrey", "dark grey", "aqua", "fuchsia", "lime", "maroon",
            "navy", "olive", "purple", "silver", "teal"))

    private var slideComponentCount = 0///////////////////////////////////////////////////////////////////////////////////////////////////
    
    
    /////***********************************//////////////////////////////////////
    var initialDownloadUrl:String ="http://"

    var downloadId:Long = 0/////ID of current Download///////////////////////////////////////////////////////


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        setContentView(R.layout.activity_main)
        // Hide the status bar.
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        // Remember that you should never show the action bar if the
        // status bar is hidden, so hide that too if necessary.
        //actionBar?.hide()
        window.decorView.systemUiVisibility = SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                SYSTEM_UI_FLAG_FULLSCREEN or SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                SYSTEM_UI_FLAG_LAYOUT_STABLE or SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        supportActionBar?.hide()




        //var wv:WebView;
        //wv.loadData()
         val dm:DownloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager


/////////////////*************************set Paths*********************************/////////////////////////
        setPaths()//***********************************************

        /////////*******************/////////////
        val idFile: String? = readJsonfile(downloadPath,"id.txt")
        if (idFile==null){
            Toast.makeText(this,"No Valid ID found", Toast.LENGTH_LONG).show()
            return
        }

        val idGson = Gson().fromJson(idFile, idDataClass::class.java)
        val deviceID = idGson.id
        Log.d("Device ID is blabla",deviceID)




//////////////////**************processJsonFile*****************************////////////////////////////
        processJsonFile()//**********************************************

////////////////////////*******************Download************************////////////////////////////////////////
        sendHeartBeatFuntion(deviceID,dm)
        //checkHeartBeat(deviceID)


        ////***************SetONCLickListener to know device id*********////////////
        /*rootLayout.setOnLongClickListener(OnLongClickListener {
            Toast.makeText(this@MainActivity, "Device ID: $deviceID", Toast.LENGTH_LONG).show()
            true
        })*/



    }

    private fun sendHeartBeatFuntion(deviceID: String,dm:DownloadManager) {
        heartBeatTimer = object: CountDownTimer(Long.MAX_VALUE, 5000) {
            override fun onTick(millisUntilFinished: Long) {
               checkHeartBeat(deviceID,dm)
            }

            override fun onFinish() {
            }
        }
        heartBeatTimer.start()
    }

    private fun afterDownloadComplete(deviceID: String) {

                Log.d("DOWNS11", "Download Done ")
                if (currentFilesPath.exists()  ){
                    val archiveDir = archivePath
                    archiveDir.deleteRecursively()
                    //FileUtils.cleanDirectory(archiveDir)/////////////////////clear archive folder
                    File(currentFilesPath.path).let { sourceFile ->
                        sourceFile.copyRecursively(File(archivePath.path), true) //now move previous currentfiles folder  to archive
                        //sourceFile.deleteRecursively()       //delete the currentFiles Folder
                    }
                    Log.d("move to archive ", "Done")

                }
                if (File(File(getExternalFilesDir(null)!!, "download"), zipFileName).isFile){
                    currentFilesPath.deleteRecursively()///// //delete the currentFiles Folder
                    Log.d("checkDownloadTimer", "valid zipFile exist")
                    Toast.makeText(this@MainActivity, "Zip Found",Toast.LENGTH_SHORT).show()
                    zipFilePath = File(File(getExternalFilesDir(null)!!, "download"), zipFileName)

                    val unZippingSuccess = unzip(zipFilePath, currentFilesPath)
                    if (unZippingSuccess){
                        zipFilePath.deleteRecursively()
                        processJsonFile()////////////////////////////////////after each download+extract
                        Toast.makeText(this@MainActivity, "Unzip Zip Done",Toast.LENGTH_SHORT).show()
                        sendDigisignUpdated(deviceID)
                    }
                    else{
                        if (File(File(getExternalFilesDir(null)!!, "download"), zipFileName).exists()){
                            File(File(getExternalFilesDir(null)!!, "download"), zipFileName).deleteRecursively()///delete the current download
                        }
                        archivePath.copyRecursively(currentFilesPath,true)///if unzip fails----bring back currentFiles from Archive
                    }


                }
        }


    private fun sendDigisignUpdated(deviceID: String) {
        UpdateAPI().sendUpdate(deviceID).enqueue(object :
            retrofit2.Callback<ResponseBody>{
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                Log.d("Update1","success")
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.d("Update1","fail")
            }

        }
        )


    }

    private fun checkHeartBeat(deviceID:String, dm:DownloadManager) {
                HeartBeatAPI().checkHeartBeat(deviceID).enqueue(object :
                    retrofit2.Callback<ResponseBody> {
                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response: Response<ResponseBody>
                    ) {

                        var recentURL = response.body()!!.string().toString()
                        if (recentURL.isNotEmpty()) {
                            if (recentURL == initialDownloadUrl) {
                                //initialDownloadUrl = response.body()!!.string()
                                Log.d("CHECK1 same", recentURL.toString())
                                //downloadFiles(response.body()!!.string().toString())
                            }
                            else{
                                Log.i("CHECK12 start",recentURL)
                                downloadFiles(deviceID,recentURL,dm)
                                Toast.makeText(this@MainActivity,recentURL,Toast.LENGTH_LONG).show()
                                initialDownloadUrl  = recentURL

                            }

                        }
                        else{
                            Log.d("CHECK1 null url","null url")
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        Log.d("CHECK1", "Error")
                    }

                }
                )
    }


    @Throws(IOException::class)
    fun unzip(zipFile: File?, targetDirectory: File?): Boolean {
        val zis = ZipInputStream(
                BufferedInputStream(FileInputStream(zipFile)))
        try {
            var ze: ZipEntry? = null////////******very Important  ze can be null now
            var count: Int
            val buffer = ByteArray(8192)
            while (zis.nextEntry.also { ze = it } != null) {
                Log.d("zeSize", ze?.compressedSize.toString())
                val file = File(targetDirectory, ze?.name)
                val dir = if (ze?.isDirectory == true) file else file.parentFile
                if (!dir.isDirectory && !dir.mkdirs()) throw FileNotFoundException("Failed to ensure directory: " +
                        dir.absolutePath)
                if (ze?.isDirectory == true) continue
                val fout = FileOutputStream(file)
                try {
                    while (zis.read(buffer).also { count = it } != -1) fout.write(buffer, 0, count)
                } finally {
                    fout.close()
                }
                /* if time should be restored as well
            long time = ze.getTime();
            if (time > 0)
                file.setLastModified(time);
            */

            }

        } finally {
            zis.close()
        }
        return true
    }



    private fun setPaths() {
        path = getExternalFilesDir(null)!!
        if(!File(path, "download").exists()) {
            val folder = File(path, "download")
            folder.mkdir()
        }
        path = File(path, "download")
        downloadPath = path////////////////////**************************************
        if(!File(path, "CurrentFiles").exists()) {
            val folder = File(path, "CurrentFiles")
            folder.mkdir()
        }
        if(!File(File(getExternalFilesDir(null)!!, "download"), "archive").exists()){
            val folder = File(File(getExternalFilesDir(null)!!, "download"), "archive")
            folder.mkdir()
        }
        archivePath = File(File(getExternalFilesDir(null)!!, "download"), "archive")
        //jsonPath = File(getExternalFilesDir(null)!!, "download")
        currentFilesPath = File(File(getExternalFilesDir(null)!!, "download"), "CurrentFiles")
        jsonPath = currentFilesPath
        //zipFilePath = File(File(getExternalFilesDir(null)!!,"download"),zipFileName)
        path = File(File(getExternalFilesDir(null)!!, "download"), "CurrentFiles")////////////////
        ///////..............................later case..........................
        Log.i("path", path.path)
    }

    private fun processJsonFile() {/////process the json and set Screens on screenTimer interval
        if(readJsonfile(jsonPath, jsonFileName)==null){
            Log.d("jsonError", "error")
            return
        }
        gsonData = Gson().fromJson(readJsonfile(jsonPath, jsonFileName), jsonDataClass::class.java)
        Log.i("Gson Data", gsonData.resolution)

        /////*****set BackGrond Color*****////////////


        for(screen in gsonData.screens){
            slideComponentCount = 0
            for (comp in screen.components){
                if(comp.type=="SLIDE"){
                    slideComponentCount++
                    comp.type = "SLIDE$slideComponentCount"
                }
            }
        }

        for (screen in gsonData.screens) {///////////////////////////////////////////////////////////////////////////////////////////////////
            screenIdList.add(screen.id)
        }

        noOfScreen = gsonData.screens.size
        activeScreenId = screenIdList[0]
        projectScreen(path, gsonData, activeScreenId)//////////////////////////commented////////////////////////////////////////////////////////////////////
        //projectAudio(path,gsonData,screenId)///////////////////////////////////////////////////////////////////////////////////////
        Log.i("Path", path.toString())

        screenTimer = object: CountDownTimer(Long.MAX_VALUE, gsonData.screenTimer * 1000) {
            override fun onTick(millisUntilFinished: Long) {
                screenIdListIterator++
                screenIdListIterator = (screenIdListIterator % noOfScreen)
                rootLayout.removeAllViews()


                cancelAllSlideTimer()

                projectScreen(path, gsonData, screenIdList[screenIdListIterator])////////////////////////commented///////////////////////////////////////////////
            }

            override fun onFinish() {
            }
        }
        screenTimer.start()///////////////////Commented for now///////////////////////////////////////////////////////////////////////////////////////////////


    }

    private fun downloadFiles(deviceID: String, url:String,dm: DownloadManager) {


        if (File(File(getExternalFilesDir(null)!!, "download"), zipFileName).exists()){
            File(File(getExternalFilesDir(null)!!, "download"), zipFileName).deleteRecursively()///delete the current download
        }


        Log.d(" DOWNS11 removed",dm.remove(downloadId).toString()+ " id = "+ downloadId.toString())///////////////////////remove current download if a new file comes

        if (File(File(getExternalFilesDir(null)!!, "download"), zipFileName).exists()){
            File(File(getExternalFilesDir(null)!!, "download"), zipFileName).deleteRecursively()///delete the current download
        }

        val request = DownloadManager.Request(
                Uri.parse(url.toString()))
                .setTitle("Files")
                .setDescription("Associated files Downloading")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                .setDestinationInExternalFilesDir(this, "download", zipFileName)
        //val dm:DownloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        downloadId = dm.enqueue(request)
        Log.d("DOWNS11 start ID",downloadId.toString())

        val br = object:BroadcastReceiver(){
            override fun onReceive(context: Context?, intent: Intent?) {
                 var checkDownloadId= intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if(checkDownloadId==downloadId){
                    Toast.makeText(this@MainActivity, "Download Complete", Toast.LENGTH_SHORT).show()
                    Log.d("checkDownloadTimer", "download complete")
                    downloadSuccess = true
                    initialDownloadUrl = url
                    Log.d("CHECK12", initialDownloadUrl!!)
                    afterDownloadComplete(deviceID)
                }
            }

        }///////....................BroadcastReceiver ends
        registerReceiver(br, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }






    ////////////////////////////////////////////////////********************************Download*********************************////////////////////////////////////////////////

    private fun cancelAllSlideTimer() {
        if (::slideImageTimer1.isInitialized) {
            slideImageTimer1.cancel()
        }
        if (::slideImageTimer2.isInitialized) {
            slideImageTimer2.cancel()
        }
        if (::slideImageTimer3.isInitialized) {
            slideImageTimer3.cancel()
        }
        if (::slideImageTimer4.isInitialized) {
            slideImageTimer4.cancel()
        }
        if (::slideImageTimer5.isInitialized) {
            slideImageTimer5.cancel()
        }
        if (::slideImageTimer6.isInitialized) {
            slideImageTimer6.cancel()
        }
        if (::slideImageTimer7.isInitialized) {
            slideImageTimer7.cancel()
        }
        if (::slideImageTimer8.isInitialized) {
            slideImageTimer8.cancel()
        }
        if (::slideImageTimer9.isInitialized) {
            slideImageTimer9.cancel()
        }
        if (::slideImageTimer10.isInitialized) {
            slideImageTimer10.cancel()
        }
    }

    private fun checkValidColor(color: String): Boolean {
        return possibleColorStrings.contains(color)
    }

    private fun imageReader(root: File): Array<File>? {
        val imageFileList: ArrayList<File> = ArrayList()
        val listAllFiles = root.listFiles()

        if (listAllFiles != null && listAllFiles.isNotEmpty()) {
            for (currentFile in listAllFiles) {
                if ( currentFile.name.endsWith(".jpg")|| currentFile.name.endsWith(".png")) {
                    // File absolute path
                    //Log.e("downloadFilePath", currentFile.getAbsolutePath())
                    // File Name
                    //Log.e("downloadFileName", currentFile.getName())
                    imageFileList.add(currentFile.absoluteFile)
                }
            }
            //Log.w("fileList", "" + imageFileList.size)
        }
        return listAllFiles
    }

    private fun projectAudio(path: File, gson: jsonDataClass?, screenId: String) {
        if (gson != null) {
            for (screen in gson.screens) {
                if (screen.id == screenId) {
                    Log.i("Csize", screen.components.size.toString())
                    for (comps in screen.components) {
                        val screenPath = File(path, "screen$screenId")
                        Log.i("screenPath", screenPath.toString())

                        if (comps.type == "AUDIO") {
                            audioPlayer(
                                    File(screenPath, (comps.resource.split("/"))[0]),
                                    (comps.resource.split(
                                            "/"
                                    ))[1]
                            )
                            //audioPlayer(File(File(path,"screen1"),"5"),"aud.mp3")

                        }
                    }
                }
            }

        }

    }

    override fun onStart() {
        super.onStart()
    }


    private fun projectScreen(path: File, gson: jsonDataClass?, screenId: String) {
        Log.d("ACscreenId", screenId)
        //Toast.makeText(this,"screen ID $screenId", Toast.LENGTH_SHORT).show()

        val fadeIn: Animation = AnimationUtils.loadAnimation(this@MainActivity, R.anim.fade_in)
        val fadeOut: Animation = AnimationUtils.loadAnimation(this@MainActivity, R.anim.fade_out)


        if (gson != null) {
            for (screen in gson.screens) {
                if (screen.id == screenId) {
                    Log.i("Csize", screen.components.size.toString())
                    /////*****set BackGrond Color*****////////////
                    if (screen.backgroundColor!=null) {
                        var backGoundColor = screen.backgroundColor
                        rootLayout.setBackgroundColor(Color.parseColor(backGoundColor))
                    }



                    for (comps in screen.components) {
                        val screenPath = File(path, "screen$screenId")
                        Log.i("screenPath", screenPath.toString())

                        if (comps.type == "IMAGE") {
                            //Log.d("ALL", (comps.resource.split("/"))[0] +" " +(comps.resource.split("/"))[1])
                            createImageView(
                                    File(screenPath, (comps.resource.split("/"))[0]),
                                    (comps.resource.split(
                                            "/"
                                    ))[1],
                                    comps.width.toFloat(),
                                    comps.height.toFloat(),
                                    comps.x.toFloat(),
                                    comps.y.toFloat()
                            )
                            //createImageView(File(screenPath,(comps.resource.split("/"))[0]),(comps.resource.split("/"))[1],
                            //100.toFloat(),150.toFloat(), 200.toFloat(),200.toFloat())
                        }
                        if (comps.type == "AUDIO") {
                            //audioPlayer(File(screenPath,(comps.resource.split("/"))[0]),(comps.resource.split("/"))[1])
                            //audioPlayer(File(File(path,"screen1"),"5"),"aud.mp3")

                        }
                        if (comps.type == "VIDEO") {
                            createVideoView(
                                    File(screenPath, (comps.resource.split("/"))[0]),
                                    (comps.resource.split(
                                            "/"
                                    ))[1],
                                    comps.width.toFloat(),
                                    comps.height.toFloat(),
                                    comps.x.toFloat(),
                                    comps.y.toFloat(),
                                    comps.param.mute,
                                    comps.param.loop
                            )
                            Log.d(
                                    "vid",
                                    comps.x.toFloat().toString() + " " + comps.y.toFloat().toString()
                            )

                        }
                        if (comps.type == "BACKGROUND") {
                            setBackImage(
                                    File(screenPath, (comps.resource.split("/"))[0]),
                                    (comps.resource.split(
                                            "/"
                                    ))[1]
                            )
                        }
                        if (comps.type == "TEXT") {



                            Log.d("TEXT1","text found")
                            var textString: String
                            val fileNmae = comps.resource.split("/")[1]
                            try {
                                val textStream: InputStream = File(
                                        File(
                                                screenPath, (comps.resource.split(
                                                "/"
                                        ))[0]
                                        ), fileNmae
                                ).inputStream()
                                textString = textStream.bufferedReader().use { it.readText() }
                                Log.i("TEXT1", textString)
                                /////***NEW WebView***///////
                                Log.d("Height Width", comps.x.toString()+
                                        comps.y.toString()+
                                        comps.width.toString()+
                                        comps.height.toString())
                                createWebView(
                                    textString,
                                    comps.width,
                                    comps.height,
                                    comps.x,
                                    comps.y,
                                )
                                /////////*********/////////////
                                /*createTextView(
                                        textString,
                                        comps.x.toFloat(),
                                        comps.y.toFloat(),
                                        comps.width.toFloat(),
                                        comps.height.toFloat(),
                                        30,
                                        "red",
                                        true,
                                        true,
                                        true,
                                        false,
                                        "red"
                                )*/
                                //Log.d("TEXT1 height", webView.originalUrl.toString())
                            } catch (e: Exception) {
                                Log.i("text", e.toString())
                            }

                            //setBackImage(File(screenPath,(comps.resource.split("/"))[0]),(comps.resource.split("/"))[1] )
                        }
                        if (comps.type == "SLIDE1") {
                                val slideImagesList = imageReader(File(screenPath, (comps.resource.split("/"))[0]))
                                val slideImagesNumber = slideImagesList!!.size
                                //Log.d("no of IMG", slideImagesNumber.toString())
                                //var slideImageIterator = 1
                                //var fileNameFull = (comps.resource.split(
                                //        "/"
                               // ))[1]
                            //var fileNameFirst = fileNameFull.split(".")[0]
                            val fileExtension = "jpg"////////////////////////////////////////////////////////////////
                            var slideImageView :ImageView
                            slideImageView = createSlideImageView(
                                    File(screenPath, (comps.resource.split("/"))[0]),
                                    slideImagesList[Random.nextInt(0, slideImagesNumber)].name,
                                    comps.width.toFloat(),
                                    comps.height.toFloat(),
                                    comps.x.toFloat(),
                                    comps.y.toFloat()
                            )
                            /*if (!slideImageView.isVisible){
                                //fileExtension = "png"
                                slideImageView = createSlideImageView(
                                        File(screenPath, (comps.resource.split("/"))[0]),
                                        "$slideImageIterator.png",
                                        comps.width.toFloat(),
                                        comps.height.toFloat(),
                                        comps.x.toFloat(),
                                        comps.y.toFloat()
                                )
                            }*/
                            //Log.d("SlideImageTimercheck",slideImageIterator.toString())

                            slideImageTimer1 = object: CountDownTimer(gson.screenTimer * 1000, comps.param.slideTime * 1000) {
                                override fun onTick(millisUntilFinished: Long) {
                                    //slideImageIterator++
                                    //slideImageIterator = (slideImageIterator % slideImagesNumber)
                                    //slideImageIterator = Random.nextInt(1, slideImagesNumber)
                                    slideImageView.startAnimation(fadeOut)
                                    rootLayout.removeView(slideImageView)
                                    slideImageView = createSlideImageView(
                                            File(screenPath, (comps.resource.split("/"))[0]),
                                            slideImagesList[Random.nextInt(0, slideImagesNumber)].name,
                                            comps.width.toFloat(),
                                            comps.height.toFloat(),
                                            comps.x.toFloat(),
                                            comps.y.toFloat()
                                    )
                                    /*if (!slideImageView.isVisible){
                                        //fileExtension = "png"
                                        slideImageView = createSlideImageView(
                                                File(screenPath, (comps.resource.split("/"))[0]),
                                                "$slideImageIterator.png",
                                                comps.width.toFloat(),
                                                comps.height.toFloat(),
                                                comps.x.toFloat(),
                                                comps.y.toFloat()
                                        )
                                    }*/
                                    slideImageView.startAnimation(fadeIn)
                                }
                                override fun onFinish() {
                                }
                            }
                            slideImageTimer1.start()
                        }
                        if (comps.type == "SLIDE2") {
                            val slideImagesList = imageReader(File(screenPath, (comps.resource.split("/"))[0]))
                            val slideImagesNumber = slideImagesList!!.size

                            //Log.d("no of IMG", slideImagesNumber.toString())
                            //var slideImageIterator = 1
                            //var fileNameFull = (comps.resource.split(
                             //       "/"
                            //))[1]
                            //var fileNameFirst = fileNameFull.split(".")[0]
                            //val fileExtension = "jpg"
                            var slideImageView :ImageView
                            slideImageView = createSlideImageView(
                                    File(screenPath, (comps.resource.split("/"))[0]),
                                    slideImagesList[Random.nextInt(0, slideImagesNumber)].name,
                                    comps.width.toFloat(),
                                    comps.height.toFloat(),
                                    comps.x.toFloat(),
                                    comps.y.toFloat()
                            )
                            /*if (!slideImageView.isVisible){
                                //fileExtension = "png"
                                slideImageView = createSlideImageView(
                                        File(screenPath, (comps.resource.split("/"))[0]),
                                        "$slideImageIterator.png",
                                        comps.width.toFloat(),
                                        comps.height.toFloat(),
                                        comps.x.toFloat(),
                                        comps.y.toFloat()
                                )
                            }*/

                            //Log.d("SlideImageTimercheck",slideImageIterator.toString())

                            slideImageTimer2 = object: CountDownTimer(gson.screenTimer * 1000, comps.param.slideTime * 1000) {
                                override fun onTick(millisUntilFinished: Long) {
                                    //slideImageIterator++
                                    //slideImageIterator = (slideImageIterator % slideImagesNumber)
                                    //slideImageIterator = Random.nextInt(1, slideImagesNumber)
                                    slideImageView.startAnimation(fadeOut)
                                    rootLayout.removeView(slideImageView)
                                    //Log.d("slideImageView11111","$slideImageIterator.$fileExtension")
                                    slideImageView = createSlideImageView(
                                            File(screenPath, (comps.resource.split("/"))[0]),
                                            slideImagesList[Random.nextInt(0, slideImagesNumber)].name,
                                            comps.width.toFloat(),
                                            comps.height.toFloat(),
                                            comps.x.toFloat(),
                                            comps.y.toFloat()
                                    )

                                    /*if (!slideImageView.isVisible){
                                        rootLayout.removeView(slideImageView)
                                        slideImageView = createSlideImageView(
                                                File(screenPath, (comps.resource.split("/"))[0]),
                                                "$slideImageIterator.png",
                                                comps.width.toFloat(),
                                                comps.height.toFloat(),
                                                comps.x.toFloat(),
                                                comps.y.toFloat()
                                        )
                                    }*/
                                    //Log.d("slideImageView","$slideImageIterator.$fileExtension"+slideImageView.isVisible.toString())
                                    slideImageView.startAnimation(fadeIn)
                                }
                                override fun onFinish() {
                                }
                            }
                            slideImageTimer2.start()
                        }
                        if (comps.type == "SLIDE3") {
                            val slideImagesList = imageReader(File(screenPath, (comps.resource.split("/"))[0]))
                            val slideImagesNumber = slideImagesList!!.size
                            //Log.d("no of IMG", slideImagesNumber.toString())
                            //var slideImageIterator = 1
                            //var fileNameFull = (comps.resource.split(
                            //       "/"
                            //))[1]
                            //var fileNameFirst = fileNameFull.split(".")[0]
                           // val fileExtension = "jpg"
                            var slideImageView :ImageView
                            slideImageView = createSlideImageView(
                                    File(screenPath, (comps.resource.split("/"))[0]),
                                    slideImagesList[Random.nextInt(0, slideImagesNumber)].name,
                                    comps.width.toFloat(),
                                    comps.height.toFloat(),
                                    comps.x.toFloat(),
                                    comps.y.toFloat()
                            )
                            /*if (!slideImageView.isVisible){
                                //fileExtension = "png"
                                slideImageView = createSlideImageView(
                                        File(screenPath, (comps.resource.split("/"))[0]),
                                        "$slideImageIterator.png",
                                        comps.width.toFloat(),
                                        comps.height.toFloat(),
                                        comps.x.toFloat(),
                                        comps.y.toFloat()
                                )
                            }*/

                            //Log.d("SlideImageTimercheck",slideImageIterator.toString())

                            slideImageTimer3 = object: CountDownTimer(gson.screenTimer * 1000, comps.param.slideTime * 1000) {
                                override fun onTick(millisUntilFinished: Long) {
                                    //slideImageIterator++
                                    //slideImageIterator = (slideImageIterator % slideImagesNumber)
                                    //slideImageIterator = Random.nextInt(1, slideImagesNumber)
                                    slideImageView.startAnimation(fadeOut)
                                    rootLayout.removeView(slideImageView)
                                    //Log.d("slideImageView11111","$slideImageIterator.$fileExtension")
                                    slideImageView = createSlideImageView(
                                            File(screenPath, (comps.resource.split("/"))[0]),
                                            slideImagesList[Random.nextInt(0, slideImagesNumber)].name,
                                            comps.width.toFloat(),
                                            comps.height.toFloat(),
                                            comps.x.toFloat(),
                                            comps.y.toFloat()
                                    )
/*
                                    if (!slideImageView.isVisible){
                                        rootLayout.removeView(slideImageView)
                                        slideImageView = createSlideImageView(
                                                File(screenPath, (comps.resource.split("/"))[0]),
                                                "$slideImageIterator.png",
                                                comps.width.toFloat(),
                                                comps.height.toFloat(),
                                                comps.x.toFloat(),
                                                comps.y.toFloat()
                                        )
                                    }*/
                                    slideImageView.startAnimation(fadeIn)
                                    //Log.d("slideImageView","$slideImageIterator.$fileExtension"+slideImageView.isVisible.toString())
                                }
                                override fun onFinish() {
                                }
                            }
                            slideImageTimer3.start()
                        }

                        if (comps.type == "SLIDE4") {
                            val slideImagesList = imageReader(File(screenPath, (comps.resource.split("/"))[0]))
                            val slideImagesNumber = slideImagesList!!.size
                            //Log.d("no of IMG", slideImagesNumber.toString())
                            //var slideImageIterator = 1
                            //var fileNameFull = (comps.resource.split(
                            //       "/"
                            //))[1]
                            //var fileNameFirst = fileNameFull.split(".")[0]
                            val fileExtension = "jpg"
                            var slideImageView :ImageView
                            slideImageView = createSlideImageView(
                                    File(screenPath, (comps.resource.split("/"))[0]),
                                    slideImagesList[Random.nextInt(0, slideImagesNumber)].name,
                                    comps.width.toFloat(),
                                    comps.height.toFloat(),
                                    comps.x.toFloat(),
                                    comps.y.toFloat()
                            )
                            /*if (!slideImageView.isVisible){
                                //fileExtension = "png"
                                slideImageView = createSlideImageView(
                                        File(screenPath, (comps.resource.split("/"))[0]),
                                        "$slideImageIterator.png",
                                        comps.width.toFloat(),
                                        comps.height.toFloat(),
                                        comps.x.toFloat(),
                                        comps.y.toFloat()
                                )
                            }*/

                            //Log.d("SlideImageTimercheck",slideImageIterator.toString())

                            slideImageTimer4 = object: CountDownTimer(gson.screenTimer * 1000, comps.param.slideTime * 1000) {
                                override fun onTick(millisUntilFinished: Long) {
                                    //slideImageIterator++
                                    //slideImageIterator = (slideImageIterator % slideImagesNumber)
                                    //slideImageIterator = Random.nextInt(1, slideImagesNumber)
                                    slideImageView.startAnimation(fadeOut)
                                    rootLayout.removeView(slideImageView)
                                    //Log.d("slideImageView11111","$slideImageIterator.$fileExtension")
                                    slideImageView = createSlideImageView(
                                            File(screenPath, (comps.resource.split("/"))[0]),
                                            slideImagesList[Random.nextInt(0, slideImagesNumber)].name,
                                            comps.width.toFloat(),
                                            comps.height.toFloat(),
                                            comps.x.toFloat(),
                                            comps.y.toFloat()
                                    )

                                    /*if (!slideImageView.isVisible){
                                        rootLayout.removeView(slideImageView)
                                        slideImageView = createSlideImageView(
                                                File(screenPath, (comps.resource.split("/"))[0]),
                                                "$slideImageIterator.png",
                                                comps.width.toFloat(),
                                                comps.height.toFloat(),
                                                comps.x.toFloat(),
                                                comps.y.toFloat()
                                        )
                                    }*/
                                    slideImageView.startAnimation(fadeIn)
                                    //Log.d("slideImageView","$slideImageIterator.$fileExtension"+slideImageView.isVisible.toString())
                                }
                                override fun onFinish() {
                                }
                            }
                            slideImageTimer4.start()
                        }

                        if (comps.type == "SLIDE5") {
                            val slideImagesList = imageReader(File(screenPath, (comps.resource.split("/"))[0]))
                            val slideImagesNumber = slideImagesList!!.size
                            //Log.d("no of IMG", slideImagesNumber.toString())
                            //var slideImageIterator = 1
                            //var fileNameFull = (comps.resource.split(
                            //       "/"
                            //))[1]
                            //var fileNameFirst = fileNameFull.split(".")[0]
                            //val fileExtension = "jpg"
                            var slideImageView :ImageView
                            slideImageView = createSlideImageView(
                                    File(screenPath, (comps.resource.split("/"))[0]),
                                    slideImagesList[Random.nextInt(0, slideImagesNumber)].name,
                                    comps.width.toFloat(),
                                    comps.height.toFloat(),
                                    comps.x.toFloat(),
                                    comps.y.toFloat()
                            )
                            /*if (!slideImageView.isVisible){
                                //fileExtension = "png"
                                slideImageView = createSlideImageView(
                                        File(screenPath, (comps.resource.split("/"))[0]),
                                        "$slideImageIterator.png",
                                        comps.width.toFloat(),
                                        comps.height.toFloat(),
                                        comps.x.toFloat(),
                                        comps.y.toFloat()
                                )
                            }*/

                            //Log.d("SlideImageTimercheck",slideImageIterator.toString())

                            slideImageTimer5 = object: CountDownTimer(gson.screenTimer * 1000, comps.param.slideTime * 1000) {
                                override fun onTick(millisUntilFinished: Long) {
                                    //slideImageIterator++
                                    //slideImageIterator = (slideImageIterator % slideImagesNumber)
                                    //slideImageIterator = Random.nextInt(1, slideImagesNumber)
                                    slideImageView.startAnimation(fadeOut)
                                    rootLayout.removeView(slideImageView)
                                    //Log.d("slideImageView11111","$slideImageIterator.$fileExtension")
                                    slideImageView = createSlideImageView(
                                            File(screenPath, (comps.resource.split("/"))[0]),
                                            slideImagesList[Random.nextInt(0, slideImagesNumber)].name,
                                            comps.width.toFloat(),
                                            comps.height.toFloat(),
                                            comps.x.toFloat(),
                                            comps.y.toFloat()
                                    )

                                    /*if (!slideImageView.isVisible){
                                        rootLayout.removeView(slideImageView)
                                        slideImageView = createSlideImageView(
                                                File(screenPath, (comps.resource.split("/"))[0]),
                                                "$slideImageIterator.png",
                                                comps.width.toFloat(),
                                                comps.height.toFloat(),
                                                comps.x.toFloat(),
                                                comps.y.toFloat()
                                        )
                                    }*/
                                    slideImageView.startAnimation(fadeIn)
                                    //Log.d("slideImageView","$slideImageIterator.$fileExtension"+slideImageView.isVisible.toString())
                                }
                                override fun onFinish() {
                                }
                            }
                            slideImageTimer5.start()
                        }

                        if (comps.type == "SLIDE6") {
                            val slideImagesList = imageReader(File(screenPath, (comps.resource.split("/"))[0]))
                            val slideImagesNumber = slideImagesList!!.size
                            //Log.d("no of IMG", slideImagesNumber.toString())
                            //var slideImageIterator = 1
                            //var fileNameFull = (comps.resource.split(
                            //       "/"
                            //))[1]
                            //var fileNameFirst = fileNameFull.split(".")[0]
                            //val fileExtension = "jpg"
                            var slideImageView :ImageView
                            slideImageView = createSlideImageView(
                                    File(screenPath, (comps.resource.split("/"))[0]),
                                    slideImagesList[Random.nextInt(0, slideImagesNumber)].name,
                                    comps.width.toFloat(),
                                    comps.height.toFloat(),
                                    comps.x.toFloat(),
                                    comps.y.toFloat()
                            )
                            /*if (!slideImageView.isVisible){
                                //fileExtension = "png"
                                slideImageView = createSlideImageView(
                                        File(screenPath, (comps.resource.split("/"))[0]),
                                        "$slideImageIterator.png",
                                        comps.width.toFloat(),
                                        comps.height.toFloat(),
                                        comps.x.toFloat(),
                                        comps.y.toFloat()
                                )
                            }*/

                            //Log.d("SlideImageTimercheck",slideImageIterator.toString())

                            slideImageTimer6 = object: CountDownTimer(gson.screenTimer * 1000, comps.param.slideTime * 1000) {
                                override fun onTick(millisUntilFinished: Long) {
                                    //slideImageIterator++
                                    //slideImageIterator = (slideImageIterator % slideImagesNumber)
                                    //slideImageIterator = Random.nextInt(1, slideImagesNumber)
                                    slideImageView.startAnimation(fadeOut)
                                    rootLayout.removeView(slideImageView)
                                    //Log.d("slideImageView11111","$slideImageIterator.$fileExtension")
                                    slideImageView = createSlideImageView(
                                            File(screenPath, (comps.resource.split("/"))[0]),
                                            slideImagesList[Random.nextInt(0, slideImagesNumber)].name,
                                            comps.width.toFloat(),
                                            comps.height.toFloat(),
                                            comps.x.toFloat(),
                                            comps.y.toFloat()
                                    )

                                    /*if (!slideImageView.isVisible){
                                        rootLayout.removeView(slideImageView)
                                        slideImageView = createSlideImageView(
                                                File(screenPath, (comps.resource.split("/"))[0]),
                                                "$slideImageIterator.png",
                                                comps.width.toFloat(),
                                                comps.height.toFloat(),
                                                comps.x.toFloat(),
                                                comps.y.toFloat()
                                        )
                                    }*/
                                    slideImageView.startAnimation(fadeIn)
                                    //Log.d("slideImageView","$slideImageIterator.$fileExtension"+slideImageView.isVisible.toString())
                                }
                                override fun onFinish() {
                                }
                            }
                            slideImageTimer6.start()
                        }

                        if (comps.type == "SLIDE7") {
                            val slideImagesList = imageReader(File(screenPath, (comps.resource.split("/"))[0]))
                            val slideImagesNumber = slideImagesList!!.size
                            //Log.d("no of IMG", slideImagesNumber.toString())
                            //var slideImageIterator = 1
                            //var fileNameFull = (comps.resource.split(
                            //       "/"
                            //))[1]
                            //var fileNameFirst = fileNameFull.split(".")[0]
                            val fileExtension = "jpg"
                            var slideImageView :ImageView
                            slideImageView = createSlideImageView(
                                    File(screenPath, (comps.resource.split("/"))[0]),
                                    slideImagesList[Random.nextInt(0, slideImagesNumber)].name,
                                    comps.width.toFloat(),
                                    comps.height.toFloat(),
                                    comps.x.toFloat(),
                                    comps.y.toFloat()
                            )
                            /*if (!slideImageView.isVisible){
                                //fileExtension = "png"
                                slideImageView = createSlideImageView(
                                        File(screenPath, (comps.resource.split("/"))[0]),
                                        "$slideImageIterator.png",
                                        comps.width.toFloat(),
                                        comps.height.toFloat(),
                                        comps.x.toFloat(),
                                        comps.y.toFloat()
                                )
                            }*/

                            //Log.d("SlideImageTimercheck",slideImageIterator.toString())

                            slideImageTimer7 = object: CountDownTimer(gson.screenTimer * 1000, comps.param.slideTime * 1000) {
                                override fun onTick(millisUntilFinished: Long) {
                                    //slideImageIterator++
                                    //slideImageIterator = (slideImageIterator % slideImagesNumber)
                                    //slideImageIterator = Random.nextInt(1, slideImagesNumber)
                                    slideImageView.startAnimation(fadeOut)
                                    rootLayout.removeView(slideImageView)
                                    //Log.d("slideImageView11111","$slideImageIterator.$fileExtension")
                                    slideImageView = createSlideImageView(
                                            File(screenPath, (comps.resource.split("/"))[0]),
                                            slideImagesList[Random.nextInt(0, slideImagesNumber)].name,
                                            comps.width.toFloat(),
                                            comps.height.toFloat(),
                                            comps.x.toFloat(),
                                            comps.y.toFloat()
                                    )

                                    /*if (!slideImageView.isVisible){
                                        rootLayout.removeView(slideImageView)
                                        slideImageView = createSlideImageView(
                                                File(screenPath, (comps.resource.split("/"))[0]),
                                                "$slideImageIterator.png",
                                                comps.width.toFloat(),
                                                comps.height.toFloat(),
                                                comps.x.toFloat(),
                                                comps.y.toFloat()
                                        )
                                    }*/
                                    slideImageView.startAnimation(fadeIn)
                                    //Log.d("slideImageView","$slideImageIterator.$fileExtension"+slideImageView.isVisible.toString())
                                }
                                override fun onFinish() {
                                }
                            }
                            slideImageTimer7.start()
                        }

                        if (comps.type == "SLIDE8") {
                            val slideImagesList = imageReader(File(screenPath, (comps.resource.split("/"))[0]))
                            val slideImagesNumber = slideImagesList!!.size
                            //Log.d("no of IMG", slideImagesNumber.toString())
                            // var slideImageIterator = 1
                            //var fileNameFull = (comps.resource.split(
                            //       "/"
                            //))[1]
                            //var fileNameFirst = fileNameFull.split(".")[0]
                            //val fileExtension = "jpg"
                            var slideImageView :ImageView
                            slideImageView = createSlideImageView(
                                    File(screenPath, (comps.resource.split("/"))[0]),
                                    slideImagesList[Random.nextInt(0, slideImagesNumber)].name,
                                    comps.width.toFloat(),
                                    comps.height.toFloat(),
                                    comps.x.toFloat(),
                                    comps.y.toFloat()
                            )
                            /*if (!slideImageView.isVisible){
                                //fileExtension = "png"
                                slideImageView = createSlideImageView(
                                        File(screenPath, (comps.resource.split("/"))[0]),
                                        "$slideImageIterator.png",
                                        comps.width.toFloat(),
                                        comps.height.toFloat(),
                                        comps.x.toFloat(),
                                        comps.y.toFloat()
                                )
                            }*/

                            //Log.d("SlideImageTimercheck",slideImageIterator.toString())

                            slideImageTimer8 = object: CountDownTimer(gson.screenTimer * 1000, comps.param.slideTime * 1000) {
                                override fun onTick(millisUntilFinished: Long) {
                                    //slideImageIterator++
                                    //slideImageIterator = (slideImageIterator % slideImagesNumber)
                                    //slideImageIterator = Random.nextInt(1, slideImagesNumber)
                                    slideImageView.startAnimation(fadeOut)
                                    rootLayout.removeView(slideImageView)
                                    //Log.d("slideImageView11111","$slideImageIterator.$fileExtension")
                                    slideImageView = createSlideImageView(
                                            File(screenPath, (comps.resource.split("/"))[0]),
                                            slideImagesList[Random.nextInt(0, slideImagesNumber)].name,
                                            comps.width.toFloat(),
                                            comps.height.toFloat(),
                                            comps.x.toFloat(),
                                            comps.y.toFloat()
                                    )

                                    /*if (!slideImageView.isVisible){
                                        rootLayout.removeView(slideImageView)
                                        slideImageView = createSlideImageView(
                                                File(screenPath, (comps.resource.split("/"))[0]),
                                                "$slideImageIterator.png",
                                                comps.width.toFloat(),
                                                comps.height.toFloat(),
                                                comps.x.toFloat(),
                                                comps.y.toFloat()
                                        )
                                    }*/
                                    slideImageView.startAnimation(fadeIn)
                                    //Log.d("slideImageView","$slideImageIterator.$fileExtension"+slideImageView.isVisible.toString())
                                }
                                override fun onFinish() {
                                }
                            }
                            slideImageTimer8.start()
                        }

                        if (comps.type == "SLIDE9") {
                            val slideImagesList = imageReader(File(screenPath, (comps.resource.split("/"))[0]))
                            val slideImagesNumber = slideImagesList!!.size
                            //Log.d("no of IMG", slideImagesNumber.toString())
                           // var slideImageIterator = 1
                            //var fileNameFull = (comps.resource.split(
                            //       "/"
                            //))[1]
                            //var fileNameFirst = fileNameFull.split(".")[0]
                            //val fileExtension = "jpg"
                            var slideImageView :ImageView
                            slideImageView = createSlideImageView(
                                    File(screenPath, (comps.resource.split("/"))[0]),
                                    slideImagesList[Random.nextInt(0, slideImagesNumber)].name,
                                    comps.width.toFloat(),
                                    comps.height.toFloat(),
                                    comps.x.toFloat(),
                                    comps.y.toFloat()
                            )
                            /*if (!slideImageView.isVisible){
                                //fileExtension = "png"
                                slideImageView = createSlideImageView(
                                        File(screenPath, (comps.resource.split("/"))[0]),
                                        "$slideImageIterator.png",
                                        comps.width.toFloat(),
                                        comps.height.toFloat(),
                                        comps.x.toFloat(),
                                        comps.y.toFloat()
                                )
                            }*/

                            //Log.d("SlideImageTimercheck",slideImageIterator.toString())

                            slideImageTimer9 = object: CountDownTimer(gson.screenTimer * 1000, comps.param.slideTime * 1000) {
                                override fun onTick(millisUntilFinished: Long) {
                                    //slideImageIterator++
                                    //slideImageIterator = (slideImageIterator % slideImagesNumber)
                                    //slideImageIterator = Random.nextInt(1, slideImagesNumber)
                                    slideImageView.startAnimation(fadeOut)
                                    rootLayout.removeView(slideImageView)
                                    //Log.d("slideImageView11111","$slideImageIterator.$fileExtension")
                                    slideImageView = createSlideImageView(
                                            File(screenPath, (comps.resource.split("/"))[0]),
                                            slideImagesList[Random.nextInt(0, slideImagesNumber)].name,
                                            comps.width.toFloat(),
                                            comps.height.toFloat(),
                                            comps.x.toFloat(),
                                            comps.y.toFloat()
                                    )

                                    /*if (!slideImageView.isVisible){
                                        rootLayout.removeView(slideImageView)
                                        slideImageView = createSlideImageView(
                                                File(screenPath, (comps.resource.split("/"))[0]),
                                                "$slideImageIterator.png",
                                                comps.width.toFloat(),
                                                comps.height.toFloat(),
                                                comps.x.toFloat(),
                                                comps.y.toFloat()
                                        )
                                    }*/
                                    slideImageView.startAnimation(fadeIn)
                                    //Log.d("slideImageView","$slideImageIterator.$fileExtension"+slideImageView.isVisible.toString())
                                }
                                override fun onFinish() {
                                }
                            }
                            slideImageTimer9.start()
                        }

                        if (comps.type == "SLIDE10") {
                            val slideImagesList = imageReader(File(screenPath, (comps.resource.split("/"))[0]))
                            val slideImagesNumber = slideImagesList!!.size
                            //Log.d("no of IMG", slideImagesNumber.toString())
                            //var slideImageIterator = 1
                            //var fileNameFull = (comps.resource.split(
                            //       "/"
                            //))[1]
                            //var fileNameFirst = fileNameFull.split(".")[0]
                            val fileExtension = "jpg"
                            var slideImageView :ImageView
                            slideImageView = createSlideImageView(
                                    File(screenPath, (comps.resource.split("/"))[0]),
                                    slideImagesList[Random.nextInt(0, slideImagesNumber)].name,
                                    comps.width.toFloat(),
                                    comps.height.toFloat(),
                                    comps.x.toFloat(),
                                    comps.y.toFloat()
                            )
                            /*if (!slideImageView.isVisible){
                                //fileExtension = "png"
                                slideImageView = createSlideImageView(
                                        File(screenPath, (comps.resource.split("/"))[0]),
                                        "$slideImageIterator.png",
                                        comps.width.toFloat(),
                                        comps.height.toFloat(),
                                        comps.x.toFloat(),
                                        comps.y.toFloat()
                                )
                            }*/

                            //Log.d("SlideImageTimercheck",slideImageIterator.toString())

                            slideImageTimer10 = object: CountDownTimer(gson.screenTimer * 1000, comps.param.slideTime * 1000) {
                                override fun onTick(millisUntilFinished: Long) {
                                    //slideImageIterator++
                                    //slideImageIterator = (slideImageIterator % slideImagesNumber)
                                    //slideImageIterator = Random.nextInt(1, slideImagesNumber)
                                    slideImageView.startAnimation(fadeOut)
                                    rootLayout.removeView(slideImageView)
                                    //Log.d("slideImageView11111","$slideImageIterator.$fileExtension")
                                    slideImageView = createSlideImageView(
                                            File(screenPath, (comps.resource.split("/"))[0]),
                                            slideImagesList[Random.nextInt(0, slideImagesNumber)].name,
                                            comps.width.toFloat(),
                                            comps.height.toFloat(),
                                            comps.x.toFloat(),
                                            comps.y.toFloat()
                                    )

                                    /*if (!slideImageView.isVisible){
                                        rootLayout.removeView(slideImageView)
                                        slideImageView = createSlideImageView(
                                                File(screenPath, (comps.resource.split("/"))[0]),
                                                "$slideImageIterator.png",
                                                comps.width.toFloat(),
                                                comps.height.toFloat(),
                                                comps.x.toFloat(),
                                                comps.y.toFloat()
                                        )
                                    }*/
                                    slideImageView.startAnimation(fadeIn)
                                    //Log.d("slideImageView","$slideImageIterator.$fileExtension"+slideImageView.isVisible.toString())
                                }
                                override fun onFinish() {
                                }
                            }
                            slideImageTimer10.start()
                        }
                    }
                }
            }

        }
    }

    private fun createWebView(text: String, w: Float, h: Float, left: Float, top: Float) {
        val webView: WebView = WebView(this)
        val settings = webView.settings;
        settings.defaultTextEncodingName = "utf-8";
        webView.isHorizontalScrollBarEnabled = false
        webView.isVerticalScrollBarEnabled = false
        webView.loadData(text, "text/html; charset=UTF-8", null)
        val paramsText = FrameLayout.LayoutParams(w.toInt(),h.toInt())
        paramsText.gravity = Gravity.NO_GRAVITY
        paramsText.leftMargin =left.toInt()
        paramsText.topMargin = top.toInt()
        webView.layoutParams = paramsText
        rootLayout.addView(webView)
        Log.d("TEXT1","Added Web")
    }

    private fun readJsonfile(path: File?, jsonFileName: String): String? {
        var jsonString: String? = null
        try {
            val inputStream: InputStream = File(path, jsonFileName).inputStream()
            jsonString = inputStream.bufferedReader().use { it.readText() }
            Log.i("Json", jsonString)
        } catch (e: Exception) {
            Log.i("Error", e.toString())
        }
        return jsonString
    }


    private fun createVideoView(
            videoDirectory: File, name: String, w: Float, h: Float,
            left: Float, top: Float, isMute: Boolean, isLoop: Boolean
    ) {
        val videoView: VideoView = VideoView(this)
        val vidFile = File(videoDirectory, name)
        val paramsVideo = FrameLayout.LayoutParams(dpToPx(w), dpToPx(h))
        Log.d("Vid too", dpToPx(top).toString())
        paramsVideo.gravity = Gravity.NO_GRAVITY
        paramsVideo.leftMargin = dpToPx(left)
        paramsVideo.topMargin = dpToPx(top)
        videoView.layoutParams = paramsVideo
        val mediaController = MediaController(this)
        mediaController.setAnchorView(videoView)
        mediaController.setMediaPlayer(videoView)
        //videoView.setMediaController(mediaController)//////////////////////no Control to stop/play/forward/backward media
        videoView.setVideoPath(vidFile.path)

        videoView.setOnPreparedListener(OnPreparedListener { mp ->

            mp.setVolume(1f, 1f)
            if (isMute) {
                mp.setVolume(0f, 0f)
            }

            mp.isLooping = true

        })
        videoView.requestFocus()
        videoView.start()
        rootLayout.addView(videoView)
    }

    private fun createImageView(
            imgDirectory: File,
            name: String,
            w: Float,
            h: Float,
            left: Float,
            top: Float
    ) {
        var imageView  = ImageView(this)
        val imgFile = File(imgDirectory, name)
        val bmImg = BitmapFactory.decodeFile(imgFile.path)
        imageView.setImageBitmap(bmImg)
        val paramsImage = FrameLayout.LayoutParams(dpToPx(w), dpToPx(h))
        Log.d("Img too", dpToPx(top).toString())
        paramsImage.gravity = Gravity.NO_GRAVITY
        paramsImage.leftMargin = dpToPx(left)
        paramsImage.topMargin = dpToPx(top)
        imageView.layoutParams = paramsImage
        rootLayout.addView(imageView)
        //rootLayout.removeView(imageView)
        //Log.d("Img", "Done")
    }
    private fun createSlideImageView(
            imgDirectory: File,
            name: String,
            w: Float,
            h: Float,
            left: Float,
            top: Float
    ): ImageView {
         var slideImageView = ImageView(this)
        val imgFile = File(imgDirectory, name)
        val bmImg = BitmapFactory.decodeFile(imgFile.path)
        slideImageView.setImageBitmap(bmImg)
        val paramsImage = FrameLayout.LayoutParams(dpToPx(w), dpToPx(h))
        //Log.d("Img too", dpToPx(top).toString())
        paramsImage.gravity = Gravity.NO_GRAVITY
        paramsImage.leftMargin = dpToPx(left)
        paramsImage.topMargin = dpToPx(top)
        slideImageView.layoutParams = paramsImage
        if(!imgFile.exists()){
            slideImageView.visibility = View.INVISIBLE
            //Log.d("slide not exist",slideImageView.isVisible.toString())
        }
        rootLayout.addView(slideImageView)
        //rootLayout.removeView(imageView)
        //Log.d("Img", "Done")

        return slideImageView
    }

    private fun createTextView(
            txt: String,
            left: Float,
            top: Float,
            w: Float,
            h: Float,
            size: Int,
            textColor: String,
            isBold: Boolean,
            isItalic: Boolean,
            isAllCaps: Boolean,
            isBackColor: Boolean,
            backColor: String

    ) {
        val textView: TextView = TextView(this)

        textView.text = txt
        val paramsText = FrameLayout.LayoutParams(dpToPx(w), dpToPx(h))
        paramsText.gravity = Gravity.NO_GRAVITY
        paramsText.leftMargin = dpToPx(left)
        paramsText.topMargin = dpToPx(top)
        textView.layoutParams = paramsText
        if (checkValidColor(textColor)){
            textView.setTextColor(textColor.toColorInt())
            Log.d("exist", textColor)
        }
        else{
            textView.setTextColor("black".toColorInt())
            Log.d("not exist", textColor)
        }
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.toFloat())
        //textView.typeface = Typeface.DEFAULT_BOLD
        if (isItalic && !isBold) {
            textView.setTypeface(null, Typeface.ITALIC)
        }
        textView.isAllCaps=isAllCaps
        if (isBold && isItalic) {
            textView.setTypeface(null, Typeface.BOLD_ITALIC)
        }
        if (isBold && !isItalic) {
            textView.setTypeface(null, Typeface.BOLD)
        }
        if (isBackColor) {
            if (checkValidColor(backColor)){
                textView.setBackgroundColor(backColor.toColorInt())
            }
            else{
                textView.setBackgroundColor("white".toColorInt())
            }
        }




        rootLayout.addView(textView)
    }

    private fun setBackImage(backImgDirectory: File, fileName: String) {
        val backBmImg = BitmapFactory.decodeFile(File(backImgDirectory, fileName).path)
        val backImage = BitmapDrawable(resources, backBmImg)
        rootLayout.background =
                backImage////////////////////////////////setting Background Image////////////////
    }

    private fun dpToPx(dp: Float): Int {
        /*val r: Resources = resources
        val px = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dip,
                r.displayMetrics
        )*/
        return dp.toInt()
    }

    private fun audioPlayer(audioDirectory: File, audioName: String) {
        val audioFile = File(audioDirectory, audioName)
        val audioMp = MediaPlayer()

        try {
            audioMp.setDataSource(audioFile.path)
            //audioMp.setDataSource()
            audioMp.isLooping = true
            //audioMp.setVolume(1f, 1f)
            audioMp.prepare()
            //audioMp.
            audioMp.start()
            //audioMp.release()

            //audioMp.reset()
        } catch (e: Exception) {
            Toast.makeText(this, "cant play Audio-${e.toString()}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> x1 = event.x

            MotionEvent.ACTION_UP -> {
                x2 = event.x
                val deltaX: Float = x2 - x1
                if (deltaX > MIN_DISTANCE_LR) {
                    //Toast.makeText(this, "left2right swipe", Toast.LENGTH_SHORT).show()
                    screenIdListIterator--
                    if (screenIdListIterator < 0) {
                        Toast.makeText(this, "This is The First Screen Available", Toast.LENGTH_SHORT).show()
                        screenIdListIterator = 0
                    } else {
                        rootLayout.removeAllViews()
                        cancelAllSlideTimer()
                        //screenTimer.cancel()
                        projectScreen(path, gsonData, screenIdList[screenIdListIterator])
                        //screenTimer.start()
                        //timer.start()
                    }

                } else if (deltaX < -MIN_DISTANCE_LR) {
                    screenIdListIterator++;
                    if (screenIdListIterator >= noOfScreen) {
                        Toast.makeText(this, "This is The Last Screen Available", Toast.LENGTH_SHORT).show()
                        screenIdListIterator = noOfScreen - 1
                    } else {
                        rootLayout.removeAllViews()
                        cancelAllSlideTimer()
                        //screenTimer.cancel()
                        projectScreen(path, gsonData, screenIdList[screenIdListIterator])
                        //screenTimer.start()
                        //timer.start()
                        //Toast.makeText(this, "right2left swipe", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        return super.onTouchEvent(event)

    }

}