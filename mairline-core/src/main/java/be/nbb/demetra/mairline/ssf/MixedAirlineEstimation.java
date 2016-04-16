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
package be.nbb.demetra.mairline.ssf;

import be.nbb.demetra.mairline.MixedAirlineModel;
import ec.demetra.realfunctions.GridSearch;
import ec.demetra.realfunctions.IFunctionMinimizer;
import ec.demetra.realfunctions.ProxyMinimizer;
import ec.demetra.ssf.dk.SsfFunction;
import ec.demetra.ssf.dk.SsfFunctionInstance;
import ec.demetra.ssf.univariate.ISsf;
import ec.demetra.ssf.univariate.SsfData;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.timeseries.simplets.TsData;

/**
 *
 * @author Jean Palate
 */
public class MixedAirlineEstimation {

    private IFunctionMinimizer minimizer = new ProxyMinimizer(new ec.demetra.realfunctions.levmar.LevenbergMarquardtMinimzer());

    public MixedAirlineModel compute(SarimaModel arima, int[] noisy, TsData s) {
        try {
            MixedAirlineModel model = new MixedAirlineModel();
            model.setAirline(arima);
            model.setNoisyPeriods(noisy);
            model.setNoisyPeriodsVariance(1);
            return searchAll(model, s, 1e-12);
        } catch (RuntimeException e) {
            return null;
        }
    }

    public MixedAirlineModel compute2(SarimaModel arima, int[] noisy, TsData s) {
        try {
            MixedAirlineModel model = new MixedAirlineModel();
            model.setAirline(arima);
            model.setNoisyPeriods(noisy);
            model.setNoisyPeriodsVariance(1);

            for (int i = 0; i < 5; ++i) {
                model = searchAirline(model, s);
                model = searchNoise(model, s);
            }
            return searchAll(model, s, 1e-12);

        } catch (RuntimeException e) {
            return null;
        }
    }

    private MixedAirlineModel searchAirline(MixedAirlineModel model, TsData s) {
        MixedAirlineMapper mapper = MixedAirlineMapper.airline(model);
        minimizer.setConvergenceCriterion(1e-6);
        SsfFunction<ISsf> fn = new SsfFunction<>(new SsfData(s), mapper, false, false);
        boolean converged = minimizer.minimize(fn);
        SsfFunctionInstance<ISsf> rfn = (SsfFunctionInstance<ISsf>) minimizer.getResult();
        return mapper.toModel(rfn.getParameters());
    }

    private MixedAirlineModel searchAll(MixedAirlineModel model, TsData s, double eps) {
        MixedAirlineMapper mapper = MixedAirlineMapper.all(model);
        minimizer.setConvergenceCriterion(eps);
        SsfFunction<ISsf> fn = new SsfFunction<>(new SsfData(s), mapper, false, false);
        boolean converged = minimizer.minimize(fn);
        SsfFunctionInstance<ISsf> rfn = (SsfFunctionInstance<ISsf>) minimizer.getResult();
        return mapper.toModel(rfn.getParameters());
    }

    private MixedAirlineModel searchNoise(MixedAirlineModel model, TsData s) {
        MixedAirlineMapper mapper = MixedAirlineMapper.noise(model);
        SsfFunction<ISsf> fn = new SsfFunction<>(new SsfData(s), mapper, false, false);
//        GridSearch grid = new GridSearch();
//        grid.setConvergenceCriterion(1e-6);
//        double a=Math.max(0, model.getNoisyPeriodsVariance()-.5);
//        double b=Math.max(0, model.getNoisyPeriodsVariance()+.5);
//        grid.setBounds(a, b);
//        boolean converged = grid.minimize(fn.evaluate(s));
        minimizer.setConvergenceCriterion(1e-6);
        minimizer.minimize(fn);
        SsfFunctionInstance<ISsf> rfn = (SsfFunctionInstance<ISsf>) minimizer.getResult();
        return mapper.toModel(rfn.getParameters());
    }

}
