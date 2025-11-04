package wawa.wkg

import android.content.Context
import android.util.Log
import androidx.work.*
import java.util.concurrent.TimeUnit


object Kawm {
    private const val UNIQUE_CYCLIC_WORK = "k_a_c_w"
    private const val UNIQUE_PERIODIC_WORK = "k_a_p_w"
    
    fun startAllTasks(context: Context) {
        try {
            startCyclicWork(context)
            startPeriodicWork(context)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startCyclicWork(context: Context) {
        try {
            val workRequest = OneTimeWorkRequestBuilder<CyclicWorker>()
                .setInitialDelay(15, TimeUnit.MINUTES) // 15分钟后执行
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresBatteryNotLow(false) // 不要求电量充足
                        .build()
                )
                .addTag(UNIQUE_CYCLIC_WORK)
                .build()
            
            WorkManager.getInstance(context).enqueueUniqueWork(
                UNIQUE_CYCLIC_WORK,
                ExistingWorkPolicy.REPLACE, // 替换已存在的任务
                workRequest
            )
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    

    private fun startPeriodicWork(context: Context) {
        try {
            val workRequest = PeriodicWorkRequestBuilder<PeriodicWorker>(
                15, TimeUnit.MINUTES // 每15分钟执行一次（最小间隔）
            )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresBatteryNotLow(false) // 不要求电量充足
                        .build()
                )
                .addTag(UNIQUE_PERIODIC_WORK)
                .build()
            
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_PERIODIC_WORK,
                ExistingPeriodicWorkPolicy.KEEP, // 保留已存在的任务
                workRequest
            )
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    

    

    class CyclicWorker(
        context: Context,
        params: WorkerParameters
    ) : Worker(context, params) {
        
        override fun doWork(): Result {
            return try {

                scheduleSelf(applicationContext)
                
                Result.success()
            } catch (e: Exception) {
                e.printStackTrace()
                
                // 即使失败也启动下一个任务
                scheduleSelf(applicationContext)
                Result.failure()
            }
        }
        

        private fun scheduleSelf(context: Context) {
            try {
                val nextWorkRequest = OneTimeWorkRequestBuilder<CyclicWorker>()
                    .setInitialDelay(15, TimeUnit.MINUTES) // 15分钟后执行下一次
                    .setConstraints(
                        Constraints.Builder()
                            .setRequiresBatteryNotLow(false)
                            .build()
                    )
                    .addTag(UNIQUE_CYCLIC_WORK)
                    .build()
                
                WorkManager.getInstance(context).enqueueUniqueWork(
                    UNIQUE_CYCLIC_WORK,
                    ExistingWorkPolicy.REPLACE,
                    nextWorkRequest
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    

    class PeriodicWorker(
        context: Context,
        params: WorkerParameters
    ) : Worker(context, params) {
        
        override fun doWork(): Result {
            return try {

                Result.success()
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure()
            }
        }
    }
}

