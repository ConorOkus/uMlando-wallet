package com.example.umlandowallet

class WritableMap {
    var json: String = "";
    var first = true;

//    fun putNull(@NonNull var1: String?)
//    fun putBoolean(@NonNull var1: String?, var2: Boolean)
//    fun putDouble(@NonNull var1: String?, var2: Double)
//    fun putInt(@NonNull var1: String?, var2: Int)

    fun putString(var1: String, var2: String?) {
        if (!first) json += ','
        json += "\"$var1\":\"$var2\""
        first = false
    }
//    fun putArray(@NonNull var1: String?, @Nullable var2: ReadableArray?)
//    fun putMap(@NonNull var1: String?, @Nullable var2: ReadableMap?)
//    fun merge(@NonNull var1: ReadableMap?)
//    fun copy(): WritableMap?

    override fun toString(): String {
        return "{$json}";
    }
}