package com.example.lr7_omr.threads

// Класс для управления потоками
class TaskManager {
    private val threads = mutableListOf<Thread>()

    fun runInThread(task: () -> Unit): Thread {
        val thread = Thread {
            try {
                task()
            } catch (e: InterruptedException) {
                println("Thread interrupted: ${e.message}")
            } finally {
                println("Завершен поток. Активно: ${activeThreadCount()-1}")
            }
        }

        thread.start()
        threads.add(thread)
        println("Запущен поток. Активно: ${activeThreadCount()}")
        return thread
    }

    //Подсчитывает, сколько потоков из списка выполняются.
    fun activeThreadCount(): Int {
        return threads.count { it.isAlive }
    }

    fun cancelAllThreads() {
        println("Отмена всех потоков")
        threads.forEach {
            if (it.isAlive) {
                it.interrupt()
            }
        }
        threads.clear()
    }


    //Singleton-реализация TaskManager
    companion object {
        val instance = TaskManager()
    }
}
