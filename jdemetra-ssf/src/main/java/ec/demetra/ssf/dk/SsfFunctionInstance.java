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
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.eco.ILikelihood;
import ec.demetra.realfunctions.IFunctionInstance;
import ec.demetra.realfunctions.ISsqFunctionInstance;
import ec.tstoolkit.utilities.Arrays2;
import static ec.demetra.ssf.dk.DkToolkit.likelihoodComputer;
import ec.demetra.ssf.univariate.ISsf;
import ec.tstoolkit.data.ReadDataBlock;
import ec.demetra.realfunctions.IFunction;
import ec.demetra.realfunctions.ISsqFunction;

/**
 *
 * @author Jean Palate
 * @param <S>
 */
public class SsfFunctionInstance<S extends ISsf> implements
        ISsqFunctionInstance, IFunctionInstance {

    /**
     *
     */
    private final S current;

    /**
     *
     */
    private final ILikelihood ll;
    private final DataBlock p;
    private final boolean ml, log;
    private double[] E;
    private final SsfFunction<S> fn;

    /**
     *
     * @param fn
     * @param p
     */
    public SsfFunctionInstance(SsfFunction<S> fn, IReadDataBlock p) {
        this.fn = fn;
        this.p = new DataBlock(p);
        this.ml = fn.ml;
        this.log = fn.log;
        current = fn.mapper.map(p);
        ll = likelihoodComputer(true, true).compute(current, fn.data);
    }

    public S getSsf() {
        return current;
    }

    @Override
    public IReadDataBlock getE() {
        if (E == null) {
            double[] res = ll.getResiduals();
            if (res == null) {
                return null;
            } else {
                E = Arrays2.compact(res);
                if (ml) {
                    double factor = Math.sqrt(ll.getFactor());
                    for (int i = 0; i < E.length; ++i) {
                        E[i] *= factor;
                    }
                }
            }
        }
        return new ReadDataBlock(E);
    }

    /**
     *
     * @return
     */
    public ILikelihood getLikelihood() {
        return ll;
    }

    @Override
    public IReadDataBlock getParameters() {
        return p;
    }

    @Override
    public double getSsqE() {
        if (ll == null) {
            return Double.NaN;
        }
        return ml ? ll.getSsqErr() * ll.getFactor() : ll.getSsqErr();
    }

    @Override
    public double getValue() {
        if (ll == null) {
            return Double.NaN;
        }
        if (log) {
            return ml ? -ll.getLogLikelihood() : Math.log(ll.getSsqErr());
        } else {
            return ml ? ll.getSsqErr() * ll.getFactor() : ll
                    .getSsqErr();
        }
    }

    @Override
    public ISsqFunction getSsqFunction() {
        return fn;
    }

    @Override
    public IFunction getFunction() {
        return fn;
    }
}
