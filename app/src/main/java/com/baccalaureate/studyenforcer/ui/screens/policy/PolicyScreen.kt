package com.baccalaureate.studyenforcer.ui.screens.policy

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.baccalaureate.studyenforcer.ui.components.AppleCard
import com.baccalaureate.studyenforcer.ui.components.HeaderBar
import com.baccalaureate.studyenforcer.ui.theme.CanvasAlabaster
import com.baccalaureate.studyenforcer.ui.theme.InkSpaceGray
import com.baccalaureate.studyenforcer.ui.theme.InkTitanium

@Composable
fun PolicyScreen(onBackPressed: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CanvasAlabaster)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp)
    ) {
        HeaderBar(
            title = "سياسة الاستخدام والانضباط",
            subtitle = "المعايير المعتمدة لضمان تركيز طالب الثانوية العامة",
            trailingContent = {
                IconButton(onClick = onBackPressed) {
                    Icon(
                        imageVector = Icons.Outlined.ArrowBack,
                        contentDescription = "رجوع",
                        tint = InkTitanium
                    )
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            PolicySection(
                title = "1. فلسفة المنظومة",
                content = "صُمم التطبيق كنظام ذاتي لفرض الانضباط الأكاديمي والتصدي لإدمان التمرير القهري على منصات التواصل، بهدف توفير بيئة دراسية تضمن أقصى معدلات الاستيعاب دون تسويف."
            )

            PolicySection(
                title = "2. قواعد القفل الصارم (Focus Locker)",
                content = "عند بدء جلسة القفل (30 دقيقة إلى 4 ساعات)، تُحجب جميع التطبيقات المشتتة وتُصمت الإشعارات. لا يمكن إلغاء الجلسة مبكراً، ويستمر القفل حتى بعد إعادة تشغيل الهاتف لضمان الانضباط التام. التطبيقات المستثناة: المكالمات الهاتفية، الآلة الحاسبة، وهذا التطبيق."
            )

            PolicySection(
                title = "3. نظام العقوبة الثلاثي (3-Strikes)",
                content = "إذا أجاب الطالب على 3 أسئلة متتالية بشكل خاطئ في النوافذ المنبثقة، يتم تفعيل وضع العقوبة المؤقت لمدة 60 دقيقة. يمكن إلغاء العقوبة مبكراً بحل 3 مراجعات نحوية متتالية بنجاح."
            )

            PolicySection(
                title = "4. الخصوصية والاستقلالية المحلية",
                content = "يعمل التطبيق محلياً بالكامل (Offline-First) داخل ذاكرة الهاتف دون رفع أي بيانات شخصية أو سجلات دراسية إلى سيرفرات خارجية."
            )
        }
    }
}

@Composable
fun PolicySection(title: String, content: String) {
    AppleCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = InkTitanium
            )
            Text(
                text = content,
                fontSize = 12.sp,
                color = InkSpaceGray,
                lineHeight = 18.sp
            )
        }
    }
}
