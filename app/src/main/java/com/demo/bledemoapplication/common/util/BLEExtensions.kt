package com.demo.bledemoapplication.common.util

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService

/**
 * To get the Gatt values of each services and each characteristics
 */
fun BluetoothGatt.printGattTable(): Pair<MutableList<BluetoothGattService>?, MutableList<BluetoothGattCharacteristic>?> {
    if (services.isEmpty()) {
        return Pair(null, null)
    }
    val newServices = mutableListOf<BluetoothGattService>()
    val newCharacteristics = mutableListOf<BluetoothGattCharacteristic>()
    services.forEach { service ->
        newServices.add(service)
        service.characteristics.joinToString(separator = "\n|--", prefix = "|--") { char ->
            newCharacteristics.add(char)
            var description = "${char.uuid}: ${char.printProperties()}"
            if (char.descriptors.isNotEmpty()) {
                description += "\n" + char.descriptors.joinToString(separator = "\n|------", prefix = "|------") { descriptor ->
                    "${descriptor.uuid}: ${descriptor.printProperties()}"
                }
            }
            description
        }
    }
    return Pair(newServices, newCharacteristics)
}

/**
 * To get the gatt characteristics properties
 */
fun BluetoothGattCharacteristic.printProperties(): String = mutableListOf<String>().apply {
    if (isReadable()) add("READABLE")
    if (isWritable()) add("WRITABLE")
    if (isWritableWithoutResponse()) add("WRITABLE WITHOUT RESPONSE")
    if (isEmpty()) add("EMPTY")
}.joinToString()

fun BluetoothGattCharacteristic.isReadable(): Boolean = containsProperty(BluetoothGattCharacteristic.PROPERTY_READ)

fun BluetoothGattCharacteristic.isWritable(): Boolean = containsProperty(BluetoothGattCharacteristic.PROPERTY_WRITE)

fun BluetoothGattCharacteristic.isWritableWithoutResponse(): Boolean = containsProperty(BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)

fun BluetoothGattCharacteristic.containsProperty(property: Int): Boolean = properties and property != 0

fun BluetoothGattDescriptor.printProperties(): String = mutableListOf<String>().apply {
    if (isReadable()) add("READABLE")
    if (isWritable()) add("WRITABLE")
    if (isEmpty()) add("EMPTY")
}.joinToString()

fun BluetoothGattDescriptor.isReadable(): Boolean = containsPermission(BluetoothGattDescriptor.PERMISSION_READ)

fun BluetoothGattDescriptor.isWritable(): Boolean = containsPermission(BluetoothGattDescriptor.PERMISSION_WRITE)

fun BluetoothGattDescriptor.containsPermission(permission: Int): Boolean = permissions and permission != 0

fun ByteArray.toHexString(): String = joinToString(separator = " ", prefix = "0x") { String.format("%02X", it) }