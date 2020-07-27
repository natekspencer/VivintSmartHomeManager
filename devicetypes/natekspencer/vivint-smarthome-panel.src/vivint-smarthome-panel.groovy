/**
 *  vivint.SmartHome Panel
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
 *  1.1.0       2020-07-26      Added lock capability to enable simple integrations with SmartThings and other systems
 *
 */

metadata {
    definition (name: "vivint.SmartHome Panel", namespace: "natekspencer", author: "Nathan Spencer") {
        capability "Actuator"
        capability "Health Check"
        capability "Lock"
        capability "Power Source"
        capability "Refresh"
        capability "Security System"
        capability "Sensor"

        attribute "messages", "string"

        // command "armAway"
        // command "armStay"
        // command "disarm"
    }
    
    preferences {
        input "defaultLockCommand", "enum", title: "Default Command When \"Lock\"-ing", options: ["away", "stay"], description: "For easability, you can send a \"lock\"/\"unlock\" command to this device. This allows for simpler commands with SmartThings and other smart home integrations such as Alexa, Google Home or Home Assistant to name a few. Panel will arm \"stay\" by default unless set here."
    }

    tiles(scale: 2) {
        multiAttributeTile(name: "securitySystemStatus", type: "generic", width: 6, height: 4, canChangeIcon: false) {
            tileAttribute("device.securitySystemStatus", key: "PRIMARY_CONTROL") {
                attributeState "disarmed" , label: '${currentValue}', backgroundColor: "#12c593", icon: "st.presence.house.unlocked", action: "lock", defaultState: true
                attributeState "armedStay", label: 'armed stay'     , backgroundColor: "#e76a00", icon: "st.presence.house.secured" , action: "disarm"
                attributeState "armedAway", label: 'armed away'     , backgroundColor: "#e76a00", icon: "st.Home.home3"             , action: "disarm"
            }
            tileAttribute("device.messages", key: "SECONDARY_CONTROL") {
                attributeState("messages", label:'${currentValue}', defaultState: true)
            }
        }

        standardTile("armStay", "device.securitySystemStatus", width: 2, height: 2, decoration: "flat") {
            state "armed"   , label: 'arm stay', icon: "st.presence.house.secured", defaultState: true
            state "disarmed", label: 'arm stay', icon: "st.presence.house.secured", action: "armStay", backgroundColor: "#e76a00"
        }

        standardTile("disarm", "device.securitySystemStatus", width: 2, height: 2, decoration: "flat") {
            state "armed"   , label: 'disarm', icon: "st.presence.house.unlocked", action: "disarm", backgroundColor: "#12c593", defaultState: true
            state "disarmed", label: 'disarm', icon: "st.presence.house.unlocked"
        }

        standardTile("armAway", "device.securitySystemStatus", width: 2, height: 2, decoration: "flat") {
            state "armed"   , label: 'arm away', icon: "st.Home.home3", defaultState: true
            state "disarmed", label: 'arm away', icon: "st.Home.home3", action: "armAway", backgroundColor: "#e76a00"
        }

        standardTile("powerSource", "device.powerSource", width: 2, height: 2, decoration: "flat") {
            state "mains",   label: '${currentValue}', icon: "st.switches.switch.on"
            state "battery", label: '${currentValue}', icon: "st.samsung.da.RC_ic_charge"
            state "unknown", label: '${currentValue}', icon: "st.switches.switch.off"    , defaultState: true
        }

        standardTile("alarm", "device.alarm", width: 2, height: 2, decoration: "flat") {
            state "off"  , label: 'off'  , action: "siren", nextState: "siren", backgroundColor: "#00a0dc", icon: "st.presence.house.secured" 
            state "siren", label: 'siren', action: "off"  , nextState: "off"  , backgroundColor: "#ffffff", icon: "st.presence.house.unlocked"
        }

        valueTile("battery", "device.battery", width: 2, height: 2, decoration: "flat") {
            state "battery", label:'${currentValue}% battery'
        }

        standardTile("refresh", "device.refresh", width: 2, height: 2, decoration: "flat") {
            state "refresh", label: 'refresh', action: "refresh", icon: "st.secondary.refresh"
        }
        
        main("securitySystemStatus")
        details(["securitySystemStatus", "armStay", "disarm", "armAway", "refresh"])
    }
}

def installed() {
}

def updated() {
}

def armStay(bypassAll) {
    sendEvent(name: "securitySystemStatus", value: "armedStay")
    setArmState(3, bypassAll)
}

def disarm() {
    sendEvent(name: "securitySystemStatus", value: "disarmed")
    setArmState(0)
}

def armAway(bypassAll) {
    sendEvent(name: "securitySystemStatus", value: "armedAway")
    setArmState(4, bypassAll)
}

def setArmState(state, bypassAll=false) {
    def dni = device.deviceNetworkId.tokenize(".")
    parent.dispatchCommand(dni[0], null, "armedstates", ["armState": state])
    runIn(4, refresh)
}

def lock() {
    if (defaultLockCommand == "away")
        armAway()
    else
        armStay()
}

def unlock() {
    disarm()
}

def refresh() {
    parent.pollChildren()
}

def parseEventData(Map results) {
    results.each {name, value ->
        switch (name) {
            case "acpow":
                sendEvent(name: "powerSource", value: value ? "mains": "battery")
                break
            case "messages":
                sendEvent(name: name, value: value, displayed: false)
            case "ol":
                sendEvent(name: "DeviceWatch-DeviceStatus", value: value ? "online" : "offline", displayed: false)
                sendEvent(name: "healthStatus", value: value ? "online" : "offline", displayed: false)
                break
            case "s":
                sendEvent(name: "securitySystemStatus", value: armState(value))
                sendEvent(name: "lock", value: !!value)
                break
        }
    }
}

def armState(state) {
    [
        0:"disarmed",
        3:"armedStay",
        4:"armedAway"
    ][state]
}