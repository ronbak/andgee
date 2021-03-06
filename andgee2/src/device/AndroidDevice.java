/*
 * wiigee - accelerometerbased gesture recognition
 * Copyright (C) 2007, 2008 Benjamin Poppinga
 * 
 * Developed at University of Oldenburg
 * Contact: benjamin.poppinga@informatik.uni-oldenburg.de
 *
 * This file is part of wiigee.
 *
 * wiigee is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package device;

import android.hardware.SensorListener;
import android.hardware.SensorManager;


/**
 * Android based device implementation
 * 
 * @author liangj01
 *
 */
public class AndroidDevice extends Device implements SensorListener {

    private float x0, y0, z0, x1, y1, z1;

    public AndroidDevice() {
        // 'Calibrate' values
        this.x0 = 0;
        this.y0 = -SensorManager.STANDARD_GRAVITY;
        this.z0 = 0;
        this.x1 = SensorManager.STANDARD_GRAVITY;
        this.y1 = 0;
        this.z1 = SensorManager.STANDARD_GRAVITY;

    }
    
    @Override
    public void onAccuracyChanged(int arg0, int arg1) {
        //TODO 
    }
    
    @Override
    public void onSensorChanged(int sensor, float[] values) {


        if (this.accelerationEnabled() && sensor == SensorManager.SENSOR_ACCELEROMETER) {

            double x, y, z;
            float xraw, yraw, zraw;
                /*
                 * calculation of acceleration vectors starts here. further
                 * information about normation exist in the public papers or
                 * the various www-sources.
                 * 
                 */
                xraw = values[SensorManager.DATA_X];
                yraw = values[SensorManager.DATA_Y];
                zraw = values[SensorManager.DATA_Z];

                x = (double) (xraw - x0) / (double) (x1 - x0);
                y = (double) (yraw - y0) / (double) (y1 - y0);
                z = (double) (zraw - z0) / (double) (z1 - z0);

                this.fireAccelerationEvent(new double[] {x, y, z});
        } 

    }
    

}
