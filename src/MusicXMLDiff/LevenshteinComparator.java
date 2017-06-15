package MusicXMLDiff;

import MusicXMLParser.MusicXMLFile;

import java.util.ArrayList;

public class LevenshteinComparator {

    private ArrayList<String> mErrorLog;
    private MusicXMLFile mGroundTruth;
    private boolean mPrintLogs = false;

    public LevenshteinComparator(MusicXMLFile groundTruth) {
        mGroundTruth = groundTruth;
    }

    public LevenshteinComparator(MusicXMLFile groundTruth, boolean printLogs) {
        mGroundTruth = groundTruth;
        mPrintLogs = printLogs;
    }

    public void setLogging(boolean loggingEnabled) {
        mPrintLogs = loggingEnabled;
    }

    public int editDistance(MusicXMLFile evaluated) {
        int[][] distanceMatrix = new int[mGroundTruth.length() + 1][evaluated.length() + 1];

        for (int i = 0; i <= mGroundTruth.length(); i++)
            distanceMatrix[i][0] = i;
        for (int i = 0; i <= evaluated.length(); i++)
            distanceMatrix[0][i] = i;

        for (int i = 1; i <= mGroundTruth.length(); i++) {
            int substitutionCost;
            for (int j = 1; j <= evaluated.length(); j++) {
                if (evaluated.getElement(j - 1).equals(mGroundTruth.getElement(i - 1)))
                    substitutionCost = 0;
                else
                    substitutionCost = 1;
                //substitutionCost = symbolDistance(a.getElement(i - 1), b.getElement(j - 1));
                distanceMatrix[i][j] = minimum(
                        distanceMatrix[i - 1][j] + 1, // could be +5 with substitutionCost varying from 0 to 5
                        distanceMatrix[i][j - 1] + 1,
                        distanceMatrix[i - 1][j - 1] + substitutionCost
                );
            }
        }
        if (mPrintLogs) {
            mErrorLog = new ArrayList<>();
            createBackTrace(distanceMatrix, evaluated, mGroundTruth.length(), evaluated.length());
            printBackTrace();
        }
        return distanceMatrix[mGroundTruth.length()][evaluated.length()];
    }

    private void createBackTrace(int[][] distanceMatrix, MusicXMLFile evaluated, int x, int y) {
        if (x == 0 && y == 0)
            return ;
        int current = distanceMatrix[x][y];
        int minimum = minimum(distanceMatrix[x - 1][y - 1], distanceMatrix[x - 1][y], distanceMatrix[x][y - 1]);
        if (minimum < current) {
            if (distanceMatrix[x - 1][y - 1] == minimum) {
                mErrorLog.add("Substitution of " + mGroundTruth.getElement(x - 1) + " with " + evaluated.getElement(y - 1));
                createBackTrace(distanceMatrix, evaluated, x - 1, y - 1);
            } else if (distanceMatrix[x - 1][y] == minimum) {
                mErrorLog.add("Deletion of " + mGroundTruth.getElement(x - 1));
                createBackTrace(distanceMatrix, evaluated, x - 1, y);
            } else if (distanceMatrix[x][y - 1] == minimum) {
                mErrorLog.add("Addition of " + evaluated.getElement(y - 1));
                createBackTrace(distanceMatrix, evaluated, x, y - 1);
            }
        }
        else
            createBackTrace(distanceMatrix, evaluated, x - 1, y - 1);
    }

    private void printBackTrace() {
        for (int i = mErrorLog.size() - 1; i >= 0; --i) {
            System.out.println(mErrorLog.get(i));
        }
    }

    private int minimum(int a, int b, int c) {
        return Math.min(Math.min(a, b), c);
    }
}