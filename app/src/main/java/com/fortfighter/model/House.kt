package com.fortfighter.model

import androidx.annotation.DrawableRes
import com.fortfighter.R
import com.google.firebase.firestore.DocumentId

class House(
    @DocumentId val id: String = "",
    val type: Long = 0,
    val status: Long = 0,
    val hp: Long = 0,
    val cp: Long = 0,
    val repairCount: Long = 0,
    val cleanCount: Long = 0
) {
    // Get tool data depending on its type
    fun getHouseStaticData() = mapOf(
        0 to HouseStaticData(
            "Bangsal Kencono",
            "Bangsal Kencono Jogja",
            "Jawa",
            240,
            600,
            "Rumah adat berbentuk joglo ini merupakan salah satu kebudayaan Yogyakarta yang dibangun oleh Sultan Hamengku Buwono I tahun 1756 di kompleks keraton Yogyakarta.",
            "Bangsal Kencono adalah rumah tradisional masyarakat Jawa atau daerah lain di Indonesia yang terdiri atas 4 tiang utama.\n" +
                    "Rumah tradisional Jawa terbagi menjadi dua bagian, yakni rumah induk dan rumah tambahan. Rumah induk terdiri dari beberapa bagian sebagai berikut:\n" +
                    "1. Pendapa/Pendopo. Bagian ini terletak di depan rumah. Biasanya digunakan untuk aktivitas formal, seperti pertemuan, tempat pagelaran seni wayang kulit dan tari-tarian, serta upacara adat. Ruang ini menunjukkan sikap akrab dan terbuka, meskipun begitu Pendopo sering kali dibuat megah dan berwibawa.\n" +
                    "2. Pringgitan. Bagian ini terletak antara pendapa dan rumah dalam (omah njero). Selain digunakan untuk jalan masuk, lorong juga kerap digunakan sebagai tempat pertunjukan wayang kulit. Bentuk dari pringitan seperti serambi berbentuk tiga persegi dan menghadap ke arah pendopo.\n" +
                    "3. Emperan. Ini adalah penghubung antara pringitan dan umah njero. Bisa juga dikatakan sebagai teras depan karena lebarnya sekitar 2 meter. Emperan digunakan untuk menerima tamu, tempat bersantai, dan kegiatan publik lainnya. Pada emperan biasanya terdapat sepasang kursi kayu dan meja.\n" +
                    "4. Omah dalem. Bagian ini sering pula disebut omah mburi, dalem ageng, atau omah saja. Kadang disebut juga sebagai omah-mburi, dalem ageng atau omah. Kata omah dalam masyarakat Jawa juga digunakan sebagai istilah yang mencakup arti kedomestikan, yaitu sebagai sebuah unit tempat tinggal.\n" +
                    "5. Senthong-kiwa. Berada di sebelah kiri dan terdiri dari beberapa ruangan. Ada yang berfungsi sebagai kamar tidur, gudang, tempat menyimpan persediaan makanan, dan lain sebagainya.\n" +
                    "6. Senthong tengah. Bagian ini terletak ditengah bagian dalam. Sering juga disebut pedaringan, boma, atau krobongan. Sesuai dengan letaknya yang berada jauh di dalam rumah, bagian ini berfungsi sebagai tempat menyimpan benda-benda berharga, seperti harta keluarga atau pusaka semacam keris, dan lain sebagainya.\n" +
                    "7. Senthong-tengen. Bagian ini sama seperti Senthong kiwa, baik fungsinya maupun pembagian ruangannya.\n" +
                    "8. Gandhok. Merupakan bangunan tambahan yang letaknya mengitari sisi belakang dan samping bangunan inti.",
            R.drawable.img_game_house_0_intact,
            R.drawable.img_game_house_0_damaged,
            R.drawable.img_info_house_0
        )
    )[type.toInt()]
}

data class HouseStaticData(
    val name: String,
    val nameLong: String,
    val island: String,
    val maxHp: Int,
    val maxCP: Int,
    val descriptionShort: String,
    val descriptionLong: String,
    @DrawableRes val houseIntactImageResId: Int,
    @DrawableRes val houseDamagedImageResId: Int,
    @DrawableRes val infoImageResId: Int
)