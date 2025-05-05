public class VESolverFactory {
    public static VESolver getSolver(int type) {
        switch (type) {
            case 1: return new VEType1Solver();
            case 2: return new VEType2Solver();
            case 3: return new VEType3Solver();
            default: throw new IllegalArgumentException("Unknown VE type: " + type);
        }
    }
}
