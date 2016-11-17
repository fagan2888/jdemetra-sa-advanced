/*
 * Copyright 2016 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package be.nbb.demetra.stl;

import data.Data;
import ec.tstoolkit.data.DataBlock;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class StlPlusTest {

    public StlPlusTest() {
    }

    @Test
    public void testDefault() {

        StlPlus stl = new StlPlus(12, 7);
        stl.setNo(5);
        stl.process(Data.X);
        System.out.println(new DataBlock(stl.trend));
        System.out.println(new DataBlock(stl.season[0]));
        System.out.println(new DataBlock(stl.irr));
    }

    @Test
    public void stressTest() {
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < 10000; ++i) {
            StlPlus stl = new StlPlus(12, 7);
          stl.setNo(5);
          stl.process(Data.X);
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
    }
}
