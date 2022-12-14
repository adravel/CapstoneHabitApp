package com.fortfighter.benchmark

import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Benchmark {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun benchmarkHomeStartup() = benchmarkRule.measureRepeated(
        packageName = "com.fortfighter",
        metrics = listOf(StartupTimingMetric()),
        iterations = 10,
        startupMode = StartupMode.COLD,
        setupBlock = {
            loginAsParent()
        }
    ) {
        pressHome()
        startActivityAndWait()
    }

    @Test
    fun benchmarkTaskListFrame() = benchmarkRule.measureRepeated(
        packageName = "com.fortfighter",
        metrics = listOf(FrameTimingMetric()),
        iterations = 10,
        setupBlock = {
            loginAsParent()

            if (device.hasObject(By.res(packageName, "task_menu_card"))) {
                device.findObject(By.res(packageName, "task_menu_card")).click()
                device.wait(Until.hasObject(By.hasChild(By.res(packageName, "task_list_recycler_view"))), 5000)
            }
        }
    ) {
        val view = device.findObject(By.res(packageName, "scroll_view_layout"))
        view.setGestureMargin(device.displayWidth / 5)
        view.fling(Direction.DOWN)
        device.waitForIdle()
        view.fling(Direction.UP)
        device.waitForIdle()
    }

    private fun MacrobenchmarkScope.loginAsParent() {
        startActivityAndWait()

        if (device.hasObject(By.res(packageName, "login_button"))) {
            device.findObject(By.res(packageName, "login_button")).click()
            device.waitForIdle()

            device.findObject(By.res(packageName,"email_edit_text")).text =  "testing@mail.com"
            device.findObject(By.res(packageName,"password_edit_text")).text = "tes123"
            device.findObject(By.res(packageName,"login_button")).click()
            device.wait(Until.hasObject(By.text("Pilih Role")), 5000)

            device.findObject(By.res(packageName,"parent_role_card")).click()
            device.wait(Until.hasObject(By.text("Halo")), 5000)
        }
    }
}