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
package ec.demetra.ssf.dk;

import ec.tstoolkit.data.DataBlock;
import ec.demetra.ssf.univariate.UpdateInformation;

/**
 *
 * @author Jean Palate
 */
public class DiffuseUpdateInformation extends UpdateInformation {

    /**
     */
    private double fi;

    /**
     * Ci = Pi Z'
     */
    private final DataBlock Mi;

    /**
     *
     * @param dim
     */
    public DiffuseUpdateInformation(final int dim) {
        super(dim);
        Mi = new DataBlock(dim);
    }
    
    public DataBlock Mi(){
        return this.Mi;
    }
    
    public double getDiffuseNorm2(){
        return fi;
    }
    
     public void setDiffuseNorm2(final double n){
        fi=n;
    }

     public boolean isDiffuse(){
         return fi != 0;
     }
}
