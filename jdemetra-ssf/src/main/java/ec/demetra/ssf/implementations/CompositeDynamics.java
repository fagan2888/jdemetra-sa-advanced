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
package ec.demetra.ssf.implementations;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.maths.matrices.SubMatrix;
import ec.tstoolkit.utilities.Jdk6;
import ec.demetra.ssf.ISsfDynamics;
import ec.demetra.ssf.univariate.ISsf;
import java.util.List;

/**
 * Dynamics generated by a juxtaposition of several dynamics. The underlying
 * state is the concatenation of the original states.
 *
 * @author Jean Palate
 */
public class CompositeDynamics implements ISsfDynamics {

    private final ISsfDynamics[] dyn;
    private final int[] dim;
    private final int fdim;

    public static CompositeDynamics of(ISsf... ssfs) {
        ISsfDynamics[] dyn = new ISsfDynamics[ssfs.length];
        for (int i = 0; i < dyn.length; ++i) {
            dyn[i] = ssfs[i].getDynamics();
        }
        return new CompositeDynamics(dyn);
    }

    public static CompositeDynamics ofSsf(List<ISsf> ssfs) {
        ISsfDynamics[] dyn = new ISsfDynamics[ssfs.size()];
        int cur = 0;
        for (ISsf ssf : ssfs) {
            dyn[cur++] = ssf.getDynamics();
        }
        return new CompositeDynamics(dyn);
    }

    public CompositeDynamics(ISsfDynamics... ssfs) {
        dyn = ssfs;
        int n = ssfs.length;
        dim = new int[n];
        int tdim = ssfs[0].getStateDim();
        dim[0] = tdim;
        for (int i = 1; i < n; ++i) {
            dim[i] = ssfs[i].getStateDim();
            tdim += dim[i];
        }
        fdim = tdim;
    }

    public CompositeDynamics(List<ISsfDynamics> ssfs) {
        this(Jdk6.Collections.toArray(ssfs, ISsfDynamics.class));
    }

    @Override
    public int getStateDim() {
        return fdim;
    }

    public int getComponentsCount() {
        return dyn.length;
    }

    public ISsfDynamics getComponent(int pos) {
        return dyn[pos];
    }

    @Override
    public boolean isTimeInvariant() {
        for (int i = 0; i < dyn.length; ++i) {
            if (!dyn[i].isTimeInvariant()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isValid() {
        for (int i = 0; i < dyn.length; ++i) {
            if (!dyn[i].isValid()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int getInnovationsDim() {
        int ni = 0;
        for (int i = 0; i < dyn.length; ++i) {
            ni += dyn[i].getInnovationsDim();
        }
        return ni;
    }

    @Override
    public void V(int pos, SubMatrix qm) {
        SubMatrix cur = qm.topLeft();
        for (int i = 0; i < dyn.length; ++i) {
            cur.next(dim[i], dim[i]);
            dyn[i].V(pos, cur);
        }
    }

    @Override
    public boolean hasInnovations(int pos) {
        for (int i = 0; i < dyn.length; ++i) {
            if (dyn[i].hasInnovations(pos)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void S(int pos, SubMatrix sm) {
        SubMatrix cur = sm.topLeft();
        for (int i = 0; i < dyn.length; ++i) {
            int rcount = dim[i];
            int rdim = dyn[i].getInnovationsDim();
            cur.next(rcount, rdim);
            if (rdim > 0) {
                dyn[i].S(pos, cur);
            }
        }
    }

    @Override
    public void addSU(int pos, DataBlock x, DataBlock u) {
        DataBlock xcur = x.start(), ucur = u.start();
        for (int i = 0; i < dyn.length; ++i) {
            int rcount = dim[i];
            int rdim = dyn[i].getInnovationsDim();
            xcur.next(rcount);
            if (rdim > 0) {
                ucur.next(rdim);
                dyn[i].addSU(pos, xcur, ucur);
            }
        }
    }

    @Override
    public void XS(int pos, DataBlock x, DataBlock xs) {
        DataBlock xcur = x.start(), ycur = xs.start();
        for (int i = 0; i < dyn.length; ++i) {
            int rcount = dim[i];
            int rdim = dyn[i].getInnovationsDim();
            xcur.next(rcount);
            if (rdim > 0) {
                ycur.next(rdim);
                dyn[i].XS(pos, xcur, ycur);
            }
        }
    }

    @Override
    public void T(int pos, SubMatrix tr
    ) {
        SubMatrix cur = tr.topLeft();
        for (int i = 0, j = 0; i < dyn.length; ++i) {
            cur.next(dim[i], dim[i]);
            dyn[i].T(pos, cur);
        }
    }

    @Override
    public boolean isDiffuse() {
        for (int i = 0; i < dyn.length; ++i) {
            if (dyn[i].isDiffuse()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getNonStationaryDim() {
        int nd = 0;
        for (int i = 0; i < dyn.length; ++i) {
            nd += dyn[i].getNonStationaryDim();
        }
        return nd;
    }

    @Override
    public void diffuseConstraints(SubMatrix b) {
        // statedim * diffusedim
        SubMatrix cur = b.topLeft();
        for (int i = 0, j = 0; i < dyn.length; ++i) {
            int nst = dyn[i].getNonStationaryDim();
            if (nst != 0) {
                cur.next(dim[i], nst);
                dyn[i].diffuseConstraints(cur);
                j += nst;
            } else {
                cur.vnext(dim[i]);
            }
        }
    }

    @Override
    public boolean a0(DataBlock a0) {
        DataBlock cur = a0.start();
        for (int i = 0; i < dyn.length; ++i) {
            cur.next(dim[i]);
            if (!dyn[i].a0(cur)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean Pf0(SubMatrix p) {
        SubMatrix cur = p.topLeft();
        for (int i = 0; i < dyn.length; ++i) {
            cur.next(dim[i], dim[i]);
            if (!dyn[i].Pf0(cur)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void Pi0(SubMatrix p
    ) {
        SubMatrix cur = p.topLeft();
        for (int i = 0; i < dyn.length; ++i) {
            cur.next(dim[i], dim[i]);
            dyn[i].Pi0(cur);
        }
    }

    @Override
    public void TX(int pos, DataBlock x
    ) {
        DataBlock cur = x.start();
        for (int i = 0; i < dyn.length; ++i) {
            cur.next(dim[i]);
            dyn[i].TX(pos, cur);
        }
    }

    @Override
    public void XT(int pos, DataBlock x
    ) {
        DataBlock cur = x.start();
        for (int i = 0; i < dyn.length; ++i) {
            cur.next(dim[i]);
            dyn[i].XT(pos, cur);
        }
    }

    @Override
    public void TVT(int pos, SubMatrix v
    ) {
        SubMatrix D = v.topLeft();
        for (int i = 0; i < dyn.length; ++i) {
            int ni = dim[i];
            D.next(ni, ni);
            dyn[i].TVT(pos, D);
            SubMatrix C = D.clone(), R = D.clone();
            for (int j = i + 1; j < dyn.length; ++j) {
                int nj = dim[j];
                C.vnext(nj);
                R.hnext(nj);
                SubMatrix Ct = C.transpose();
                dyn[j].TM(pos, C);
                dyn[i].TM(pos, Ct);
                R.copy(Ct);
            }
        }
    }

    @Override
    public void addV(int pos, SubMatrix p
    ) {
        SubMatrix cur = p.topLeft();
        for (int i = 0; i < dyn.length; ++i) {
            cur.next(dim[i], dim[i]);
            dyn[i].addV(pos, cur);
        }
    }
}
