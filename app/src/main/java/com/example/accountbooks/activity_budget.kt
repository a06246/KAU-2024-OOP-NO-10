package com.example.accountbooks

import android.graphics.Color
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry

class ActivityBudget : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_budget)

        // PieChart 설정
        val pieChartTotal: PieChart = findViewById(R.id.pie_chart_total) // PieChart 아이디로 연결
        val pieChartVariable: PieChart = findViewById(R.id.pie_chart_variable)
        val pieChartFixed: PieChart = findViewById(R.id.pie_chart_fixed)

        // PieChart에 데이터 추가
        val entriesTotal = ArrayList<PieEntry>()
        entriesTotal.add(PieEntry(40f, "식비"))
        entriesTotal.add(PieEntry(25f, "교통비"))
        entriesTotal.add(PieEntry(15f, "주거비"))
        entriesTotal.add(PieEntry(20f, "기타"))

        // PieDataSet 설정
        val dataSetTotal = PieDataSet(entriesTotal, "전체 예산")
        dataSetTotal.colors = listOf(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW)

        // PieData 생성
        val dataTotal = PieData(dataSetTotal)

        // PieChart에 데이터 설정
        pieChartTotal.data = dataTotal
        pieChartTotal.setUsePercentValues(true) // 퍼센트 값 사용
        pieChartTotal.description.isEnabled = false // 설명 텍스트 비활성화
        pieChartTotal.setDrawHoleEnabled(false) // 가운데 빈 공간 비활성화
        pieChartTotal.legend.isEnabled = true // 범례 표시

        // 변동 예산 PieChart 데이터 설정 (예시)
        val entriesVariable = ArrayList<PieEntry>()
        entriesVariable.add(PieEntry(50f, "변동예산 항목1"))
        entriesVariable.add(PieEntry(50f, "변동예산 항목2"))

        val dataSetVariable = PieDataSet(entriesVariable, "변동 예산")
        dataSetVariable.colors = listOf(Color.CYAN, Color.MAGENTA)

        val dataVariable = PieData(dataSetVariable)
        pieChartVariable.data = dataVariable
        pieChartVariable.setUsePercentValues(true)
        pieChartVariable.description.isEnabled = false
        pieChartVariable.setDrawHoleEnabled(false)
        pieChartVariable.legend.isEnabled = true

        // 고정 예산 PieChart 데이터 설정 (예시)
        val entriesFixed = ArrayList<PieEntry>()
        entriesFixed.add(PieEntry(70f, "고정예산 항목1"))
        entriesFixed.add(PieEntry(30f, "고정예산 항목2"))

        val dataSetFixed = PieDataSet(entriesFixed, "고정 예산")
        dataSetFixed.colors = listOf(Color.GRAY, Color.LTGRAY)

        val dataFixed = PieData(dataSetFixed)
        pieChartFixed.data = dataFixed
        pieChartFixed.setUsePercentValues(true)
        pieChartFixed.description.isEnabled = false
        pieChartFixed.setDrawHoleEnabled(false)
        pieChartFixed.legend.isEnabled = true

        // 차트 새로 고침
        pieChartTotal.invalidate()
        pieChartVariable.invalidate()
        pieChartFixed.invalidate()

        // 시스템 바 처리
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
