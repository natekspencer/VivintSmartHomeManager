/**
 *  vivint.SmartHome Lock
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
 *
 */

metadata {
    definition (name: "vivint.SmartHome Lock", namespace: "natekspencer", author: "Nathan Spencer") {
        capability "Lock"
        capability "Battery"
        capability "Tamper Alert"
        capability "Refresh"
        capability "Health Check"
        capability "Actuator"
        capability "Sensor"
    }
    
    preferences {
    }

    tiles(scale: 2) {
        standardTile("lock", "device.lock", width: 2, height: 2, decoration: "flat") {
            state "locked"   , label: 'locked'   , action: "unlock", nextState: "unlocking", backgroundColor: "#00a0dc", icon: "st.locks.lock.locked" 
            state "unlocking", label: 'unlocking', action: "unlock", nextState: "unlocking", backgroundColor: "#ffffff", icon: "st.locks.lock.locked"
            state "unlocked" , label: 'unlocked' , action: "lock"  , nextState: "locking"  , backgroundColor: "#ffffff", icon: "st.locks.lock.unlocked", defaultState: true
            state "locking"  , label: 'locking'  , action: "lock"  , nextState: "locking"  , backgroundColor: "#00a0dc", icon: "st.locks.lock.unlocked"
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
        
        main("lock")
        details(["lock", "battery", "tamper", "refresh"])
    }
}

def installed() {
}

def updated() {
}

def lock() {
    doLock(true)
}

def unlock() {
    doLock(false)
}

def doLock(lock) {
    def dni = device.deviceNetworkId.tokenize(".")
    parent.dispatchCommand(dni[0], dni[1], "locks", ["s": lock])
    runIn(7, refresh)
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
            case "ol":
                sendEvent(name: "DeviceWatch-DeviceStatus", value: value ? "online" : "offline", displayed: false)
                sendEvent(name: "healthStatus", value: value ? "online" : "offline", displayed: false)
                break
            case "s":
                sendEvent(name: "lock", value: value ? "locked" : "unlocked")
                break
            case "ta":
                sendEvent(name: "tamper", value: value ? "detected" : "clear")
                break
        }
    }
}