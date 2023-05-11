// requires: implementation "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4"
// add above to build.gradle (Module:) file
// note the version may change



import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class CoroutinesAsyncTask<Params, Progress, Result>{

    @Suppress("PRIVATE")
    var status: Status = Status.PENDING

    abstract fun doInBackground(vararg params: Params?): Result
    open fun onProgressUpdate(vararg values: Progress?) {}
    open fun onPostExecute(result: Result?) {}
    open fun onPreExecute() {}

    @Suppress("UNUSED")
    open fun onCancelled(result: Result?) {}
    private var isCancelled = false

    fun execute(vararg params: Params){

        if (status != Status.PENDING) {
            when (status) {
                Status.RUNNING -> throw
                IllegalStateException("Cannot execute task: already running.")
                Status.FINISHED ->
                    throw IllegalStateException("Cannot execute task: already executed, a task can only execute once")
                Status.PENDING -> {}
            }
        }

        status = Status.RUNNING

        // it can be used to setup UI - should have access to Main Thread
        GlobalScope.launch(Dispatchers.Main){
            onPreExecute()
        }

        // doInBackground works on background thread(default)
        GlobalScope.launch(Dispatchers.Default){
            val result = doInBackground(*params)
            status = Status.FINISHED
            withContext(Dispatchers.Main){
                // onPostExecute works on main thread to show output
                Log.d("CoroutinesAsyncTask", "after doInBackground ${status.name}--$isCancelled")
                if (!isCancelled){onPostExecute(result)}
            }
        }
    }

    fun cancel(){
        isCancelled = true
        status = Status.FINISHED
        GlobalScope.launch(Dispatchers.Main){
            // onPostExecute works on main thread to show output
            Log.d("Alpha","after cancel "+status.name+"--"+isCancelled)
            onPostExecute(null)
        }
    }

    @Suppress("UNUSED")
    fun publishProgress(vararg progress: Progress) {
        //need to update main thread
        GlobalScope.launch(Dispatchers.Main){
            if (!isCancelled){
                onProgressUpdate(*progress)
            }
        }
    }

    companion object Constant{
        enum class Status {
            PENDING,
            RUNNING,
            FINISHED
        }
    }
}