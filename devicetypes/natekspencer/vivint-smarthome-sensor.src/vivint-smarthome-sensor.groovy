/**
 *  vivint.SmartHome Sensor
 *
 *  Copyright 2020 Nathan Spencer
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  CHANGE HISTORY
 *  VERSION     DATE            NOTES
 *  1.0.0       2020-07-21      Initial release
 *  1.0.1       2020-07-22      Added equipment code
 *
 */

 /* Equipment Type (eqt)
    CONTACT = 1;
    EMERGENCY = 11;
    FREEZE = 6;
    MOTION = 2;
    TEMPERATURE = 10;
    WATER = 8;
 */

/*  Sensor Zone Type (set)
    AUDIBLE_ALARM = 7;
    AUXILIARY_ALARM = 8;
    CARBON_MONOXIDE = 14;
    DAY_ZONE = 5;
    EXIT_ENTRY_1 = 1;
    EXIT_ENTRY_2 = 2;
    FIRE = 9;
    FIRE_WITH_VERIFICATION = 16;
    INTERIOR_FOLLOWER = 4;
    INTERIOR_WITH_DELAY = 10;
    NO_RESPONSE = 23;
    PERIMETER = 3;
    REPEATER = 25;
    SILENT_ALARM = 6;
    SILENT_BURGLARY = 24;
    UNUSED = 0;
 */

metadata {
    definition (name: "vivint.SmartHome Sensor", namespace: "natekspencer", author: "Nathan Spencer") {
        capability "Contact Sensor"
        capability "Battery"
        capability "Tamper Alert"
        capability "Refresh"
        capability "Sensor"

        attribute "equipmentCode", "string"
    }
    
    preferences {
    }

    tiles(scale: 2) {
        standardTile("contact", "device.contact", width: 2, height: 2, decoration: "flat") {
            state "closed", label: 'closed', backgroundColor: "#00a0dc", icon: "st.contact.contact.closed", defaultState: true
            state "open"  , label: 'open'  , backgroundColor: "#ffffff", icon: "st.contact.contact.open"
        }

        valueTile("battery", "device.battery", width: 2, height: 2, decoration: "flat") {
            state "battery", label:'${currentValue}% battery'
        }

        valueTile("tamper", "device.tamper", width: 2, height: 2, decoration: "flat") {
            state "tamper", label:'${currentValue}'
        }
        
        standardTile("refresh", "device.refresh", width: 2, height: 2, decoration: "flat") {
            state "refresh", label: 'refresh', action: "refresh", icon: "st.secondary.refresh"
        }
        
        main("contact")
        details(["contact", "battery", "tamper", "refresh"])
    }
}

def installed() {
}

def updated() {
}

def refresh() {
    def dni = device.deviceNetworkId.tokenize(".")
    parseEventData(parent.getDeviceData(dni[0], dni[1]))
}

def parseEventData(Map results) {
    results.each {name, value ->
        switch (name) {
            case "bl":
                sendEvent(name: "battery", value: value)
                break
            case "ec":
                sendEvent(name: "equipmentCode", value: getEquipmentCode(value))
                break
            case "s":
                sendEvent(name: "contact", value: value ? "open" : "closed")
                break
            case "ta":
                sendEvent(name: "tamper", value: value ? "detected" : "clear")
                break
        }
    }
}

def getEquipmentCode(code) {
    [
        1254: "CARBON_MONOXIDE_DETECTOR_345_MHZ",
        860 : "CO1_CO",
        859 : "CO1_CO_CANADA",
        1026: "CO3_2_GIG_CO",
        1063: "DBELL1_2_GIG_DOORBELL",
        862 : "DW10_THIN_DOOR_WINDOW",
        1251: "DW11_THIN_DOOR_WINDOW",
        863 : "DW20_RECESSED_DOOR",
        1252: "DW21_R_RECESSED_DOOR",
        692 : "EXISTING_CO",
        655 : "EXISTING_DOOR_WINDOW_CONTACT",
        556 : "EXISTING_FLOOD_TEMP",
        475 : "EXISTING_GLASS_BREAK",
        708 : "EXISTING_HEAT",
        609 : "EXISTING_MOTION_DETECTOR",
        616 : "EXISTING_SMOKE",
        1269: "FIREFIGHTER_AUDIO_DETECTOR",
        1061: "GARAGE01_RESOLUTION_TILT",
        864 : "GB1_GLASS_BREAK",
        1248: "GB2_GLASS_BREAK",
        673 : "HW_DW_5816",
        624 : "HW_FLOOD_SENSOR_5821",
        519 : "HW_GLASS_BREAK_5853",
        557 : "HW_HEAT_SENSOR_5809",
        491 : "HW_PANIC_PENDANT_5802_MN2",
        533 : "HW_PIR_5890",
        530 : "HW_PIR_5894_PI",
        470 : "HW_R_DW_5818_MNL",
        589 : "HW_SMOKE_5808_W3",
        0   : "OTHER",
        867 : "PAD1_345_WIRELESS_KEYPAD",
        868 : "PANIC1",
        1253: "PANIC2",
        869 : "PIR1_MOTION",
        1249: "PIR2_MOTION",
        1128: "RE219_FLOOD_SENSOR",
        1144: "RE220_T_2_GIG_REPEATER",
        1208: "RE224_DT_DSC_TRANSLATOR",
        941 : "RE224_GT_GE_TRANSLATOR",
        2832: "RE508_X_REPEATER",
        2830: "RE524_X_WIRELESS_TAKEOVER",
        2081: "REPEATER_345_MHZ",
        1250: "SECURE_KEY_345_MHZ",
        872 : "SMKE1_SMOKE",
        871 : "SMKE1_SMOKE_CANADA",
        895 : "SMKT2_GE_SMOKE_HEAT",
        1058: "SMKT3_2_GIG",
        1066: "SMKT6_2_GIG",
        1264: "SWS1_SMART_WATER_SENSOR",
        873 : "TAKE_TAKEOVER",
        2831: "TILT_SENSOR_2_GIG_345",
        1266: "VS_CO3_DETECTOR",
        1267: "VS_SMKT_SMOKE_DETECTOR"
    ][code]
}