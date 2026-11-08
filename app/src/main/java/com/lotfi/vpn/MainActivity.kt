package com.lotfi.vpn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

private val Night = Color(0xFF090D19)
private val Panel = Color(0xFF151C2E)
private val Teal = Color(0xFF00D6C9)
private val Violet = Color(0xFF8B7CFF)
private val Muted = Color(0xFF9AA6BD)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val store = ProfileStore(this)
        setContent { LotfiTheme { App(store) } }
    }
}

@Composable
private fun LotfiTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = darkColorScheme(primary=Teal, secondary=Violet, background=Night,
        surface=Panel, onPrimary=Night, onBackground=Color.White, onSurface=Color.White), content=content)
}

@Composable
private fun App(store: ProfileStore) {
    var tab by remember { mutableIntStateOf(0) }
    var profiles by remember { mutableStateOf(store.load()) }
    var host by remember { mutableStateOf("") }
    var port by remember { mutableStateOf("443") }
    var result by remember { mutableStateOf("أدخل نطاقاً أو خادماً تملك الإذن بفحصه.") }
    var busy by remember { mutableStateOf(false) }
    var update by remember { mutableStateOf("لم يتم فحص التحديثات بعد.") }
    val scope = rememberCoroutineScope()
    Column(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF111A32),Night,Color(0xFF070A12))))) {
        Header()
        NavigationBar(containerColor=Color(0xFF101729)) {
            val labels=listOf("الرئيسية","الخوادم","مختبر الشبكة","المزيد")
            val icons=listOf(Icons.Default.Dashboard,Icons.Default.Dns,Icons.Default.NetworkCheck,Icons.Default.Settings)
            labels.forEachIndexed { i,label ->
                NavigationBarItem(selected=tab==i,onClick={tab=i},icon={Icon(icons[i],label)},
                    label={Text(label,fontSize=10.sp)},
                    colors=NavigationBarItemDefaults.colors(selectedIconColor=Teal,selectedTextColor=Teal,
                        indicatorColor=Color(0xFF203449),unselectedIconColor=Muted,unselectedTextColor=Muted))
            }
        }
        when(tab) {
            0 -> Home(profiles.size,update,{scope.launch { update="جارٍ التحقق..."; update=UpdateChecker.check() }},{tab=1},{tab=2})
            1 -> Servers(profiles,{n,p,e -> profiles=profiles+ServerProfile(System.currentTimeMillis().toString(),n,p,e);store.save(profiles)},
                {id -> profiles=profiles.filterNot{it.id==id};store.save(profiles)})
            2 -> Lab(host,{host=it},port,{port=it},result,busy) {
                scope.launch { busy=true; result=NetworkDiagnostics.test(host.trim(),port.toIntOrNull()?:443);busy=false }
            }
            else -> More(update) { scope.launch { update="جارٍ التحقق...";update=UpdateChecker.check() } }
        }
    }
}

@Composable
private fun Header() {
    Row(Modifier.fillMaxWidth().padding(20.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceBetween) {
        Row(verticalAlignment=Alignment.CenterVertically) {
            Box(Modifier.size(48.dp).background(Brush.linearGradient(listOf(Teal,Violet)),RoundedCornerShape(15.dp)),contentAlignment=Alignment.Center) {
                Icon(Icons.Default.Shield,null,tint=Night,modifier=Modifier.size(29.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text("LOTFI VPN",fontSize=20.sp,fontWeight=FontWeight.Black,letterSpacing=1.5.sp)
                Text("ULTIMATE • NETWORK CONTROL",color=Muted,fontSize=9.sp,letterSpacing=1.sp)
            }
        }
        Text("v1.0",color=Teal,fontSize=11.sp)
    }
}

@Composable
private fun Home(count:Int, update:String, check:()->Unit, servers:()->Unit, lab:()->Unit) {
    LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(18.dp),verticalArrangement=Arrangement.spacedBy(14.dp)) {
        item {
            Card(colors=CardDefaults.cardColors(containerColor=Color(0xFF1B2944)),shape=RoundedCornerShape(24.dp)) {
                Column(Modifier.padding(22.dp)) {
                    Text("تحكّم في اتصالك",fontSize=24.sp,fontWeight=FontWeight.Bold)
                    Text("إدارة ملفات الخوادم وتشخيص الشبكة من مكان واحد.",color=Muted,fontSize=13.sp)
                    Spacer(Modifier.height(15.dp))
                    Text("محرك النفق غير مفعّل في هذه النسخة",color=Teal,fontSize=12.sp,fontWeight=FontWeight.Bold)
                    Text("لا ننشئ اتصالاً وهمياً؛ دمج المحرك الحقيقي هو المرحلة التالية.",color=Muted,fontSize=11.sp)
                }
            }
        }
        item {
            Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                Metric("الملفات المحفوظة",count.toString(),Modifier.weight(1f))
                Metric("حالة التطبيق","جاهز",Modifier.weight(1f))
            }
        }
        item { Text("أدوات سريعة",fontSize=16.sp,fontWeight=FontWeight.Bold) }
        item {
            Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                QuickCard("إدارة الخوادم","إضافة ملف اتصال",Icons.Default.Storage,servers,Modifier.weight(1f))
                QuickCard("مختبر الشبكة","DNS و TCP",Icons.Default.NetworkCheck,lab,Modifier.weight(1f))
            }
        }
        item {
            Card(colors=CardDefaults.cardColors(containerColor=Panel)) {
                Column(Modifier.padding(16.dp)) {
                    Text("التحديثات",fontWeight=FontWeight.Bold)
                    Text(update,color=Muted,fontSize=11.sp)
                    Button(onClick=check,modifier=Modifier.fillMaxWidth()) { Icon(Icons.Default.Refresh,null);Spacer(Modifier.width(8.dp));Text("فحص تحديثات GitHub") }
                }
            }
        }
        item { Text("الفحوصات مخصصة للخوادم التي تملكها أو لديك إذن باختبارها.",color=Muted,fontSize=10.sp) }
    }
}

@Composable
private fun Metric(title:String,value:String,modifier:Modifier) {
    Card(modifier,colors=CardDefaults.cardColors(containerColor=Panel)) {
        Column(Modifier.padding(16.dp)) { Text(value,fontSize=22.sp,fontWeight=FontWeight.Bold);Text(title,color=Muted,fontSize=11.sp) }
    }
}

@Composable
private fun QuickCard(title:String,sub:String,icon:androidx.compose.ui.graphics.vector.ImageVector,click:()->Unit,modifier:Modifier) {
    Card(onClick=click, modifier=modifier, colors=CardDefaults.cardColors(containerColor=Panel)) {
        Column(Modifier.padding(15.dp)) { Icon(icon,null,tint=Violet);Spacer(Modifier.height(10.dp));Text(title,fontWeight=FontWeight.Bold);Text(sub,color=Muted,fontSize=10.sp) }
    }
}

@Composable
private fun Servers(items:List<ServerProfile>,add:(String,String,String)->Unit,remove:(String)->Unit) {
    var name by remember { mutableStateOf("") }
    var endpoint by remember { mutableStateOf("") }
    var protocol by remember { mutableStateOf("WireGuard") }
    var expanded by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().padding(18.dp)) {
        Text("إدارة الخوادم",fontSize=23.sp,fontWeight=FontWeight.Bold)
        Text("أضف بيانات خادم مصرح لك باستخدامه.",color=Muted,fontSize=12.sp)
        Spacer(Modifier.height(12.dp))
        Card(colors=CardDefaults.cardColors(containerColor=Panel)) {
            Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(name,{name=it},label={Text("اسم الخادم")},modifier=Modifier.fillMaxWidth(),singleLine=true)
                OutlinedTextField(endpoint,{endpoint=it},label={Text("العنوان أو النطاق")},modifier=Modifier.fillMaxWidth(),singleLine=true)
                Box {
                    OutlinedButton(onClick={expanded=true},modifier=Modifier.fillMaxWidth()){Text("البروتوكول: $protocol")}
                    DropdownMenu(expanded,{expanded=false}) {
                        listOf("WireGuard","OpenVPN","Xray","SSH").forEach { DropdownMenuItem(text={Text(it)},onClick={protocol=it;expanded=false}) }
                    }
                }
                Button(onClick={
                    if(name.isBlank()||endpoint.isBlank()) message="أكمل الاسم والعنوان."
                    else { add(name.trim(),protocol,endpoint.trim());name="";endpoint="";message="تم حفظ بيانات الخادم." }
                },modifier=Modifier.fillMaxWidth()){Text("＋  حفظ الخادم")}
                if(message.isNotBlank()) Text(message,color=Teal,fontSize=11.sp)
            }
        }
        Spacer(Modifier.height(14.dp))
        Text("الخوادم المحفوظة (${items.size})",fontWeight=FontWeight.Bold)
        LazyColumn(verticalArrangement=Arrangement.spacedBy(8.dp)) {
            items(items,key={it.id}) { p ->
                Card(colors=CardDefaults.cardColors(containerColor=Panel)) {
                    Row(Modifier.fillMaxWidth().padding(12.dp),verticalAlignment=Alignment.CenterVertically) {
                        Icon(Icons.Default.Dns,null,tint=Teal);Spacer(Modifier.width(9.dp))
                        Column(Modifier.weight(1f)){Text(p.name,fontWeight=FontWeight.Bold);Text("${p.protocol} • ${p.endpoint}",color=Muted,fontSize=10.sp)}
                        IconButton(onClick={remove(p.id)}){Icon(Icons.Default.DeleteOutline,null,tint=Color(0xFFFF7185))}
                    }
                }
            }
        }
    }
}

@Composable
private fun Lab(host:String,onHost:(String)->Unit,port:String,onPort:(String)->Unit,result:String,busy:Boolean,test:()->Unit) {
    Column(Modifier.fillMaxSize().padding(18.dp)) {
        Text("مختبر الشبكة",fontSize=23.sp,fontWeight=FontWeight.Bold)
        Text("اختبار DNS واتصال TCP لعنوان واحد في كل مرة.",color=Muted,fontSize=12.sp)
        Spacer(Modifier.height(14.dp))
        Card(colors=CardDefaults.cardColors(containerColor=Panel)) {
            Column(Modifier.padding(15.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(host,onHost,label={Text("النطاق أو عنوان IP")},modifier=Modifier.fillMaxWidth(),singleLine=true)
                OutlinedTextField(port,onPort,label={Text("المنفذ")},modifier=Modifier.fillMaxWidth(),singleLine=true)
                Button(onClick=test,enabled=!busy,modifier=Modifier.fillMaxWidth()) {
                    if(busy) CircularProgressIndicator(Modifier.size(18.dp),strokeWidth=2.dp) else Icon(Icons.Default.PlayArrow,null)
                    Spacer(Modifier.width(8.dp));Text(if(busy)"جارٍ الاختبار..." else "ابدأ التشخيص")
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        Card(colors=CardDefaults.cardColors(containerColor=Color(0xFF101A2B))) {
            Column(Modifier.padding(15.dp)){Text("نتيجة التشخيص",color=Teal,fontWeight=FontWeight.Bold);Spacer(Modifier.height(8.dp));Text(result,color=Color(0xFFD8E0EF),fontSize=12.sp,lineHeight=19.sp)}
        }
        Spacer(Modifier.height(10.dp))
        Text("استخدم المختبر فقط على أنظمة لديك تصريح بفحصها.",color=Muted,fontSize=10.sp)
    }
}

@Composable
private fun More(update:String,check:()->Unit) {
    LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(18.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        item { Text("المزيد",fontSize=23.sp,fontWeight=FontWeight.Bold) }
        item { Info(Icons.Default.AutoAwesome,"مساعد الذكاء الاصطناعي","طبقة تشخيص أولية؛ ربط مزود AI يحتاج مفتاح API اختياري.") }
        item { Info(Icons.Default.Sync,"الأتمتة","فحص الإصدارات وإدارة ملفات الإعدادات ضمن دورة عمل قابلة للتوسعة.") }
        item { Info(Icons.Default.Security,"الأمان","لا تضع مفاتيح الخادم أو API في مستودع GitHub.") }
        item { Info(Icons.Default.VpnKey,"محرك VPN","تكامل WireGuard/Xray الفعلي مرحلة لاحقة؛ لا يوجد نفق وهمي.") }
        item { Button(onClick=check,modifier=Modifier.fillMaxWidth()){Text("فحص آخر إصدار")} }
        item { Text(update,color=Muted,fontSize=11.sp) }
    }
}

@Composable
private fun Info(icon:androidx.compose.ui.graphics.vector.ImageVector,title:String,body:String) {
    Card(colors=CardDefaults.cardColors(containerColor=Panel)) {
        Row(Modifier.fillMaxWidth().padding(15.dp),verticalAlignment=Alignment.Top) {
            Icon(icon,null,tint=Teal);Spacer(Modifier.width(12.dp))
            Column { Text(title,fontWeight=FontWeight.Bold);Text(body,color=Muted,fontSize=11.sp,lineHeight=17.sp) }
        }
    }
}
