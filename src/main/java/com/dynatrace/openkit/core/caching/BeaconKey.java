/**
 *   Copyright 2018-2021 Dynatrace LLC
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package com.dynatrace.openkit.core.caching;

public class BeaconKey {

    public final int beaconId;
    public final int beaconSeqNo;

    public BeaconKey(int beaconId, int beaconSeqNo) {
        this.beaconId = beaconId;
        this.beaconSeqNo = beaconSeqNo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BeaconKey beaconKey = (BeaconKey) o;

        if (beaconId != beaconKey.beaconId) {
            return false;
        }
        return beaconSeqNo == beaconKey.beaconSeqNo;
    }

    @Override
    public int hashCode() {
        int result = beaconId;
        result = 31 * result + beaconSeqNo;
        return result;
    }

    @Override
    public String toString() {
        return "[sn=" + beaconId + ", seq=" + beaconSeqNo + "]";
    }
}
