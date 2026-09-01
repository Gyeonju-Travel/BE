package com.example.gyeonjutravel.global.tmap;

import java.util.List;

public interface WalkingMatrixClient {

    WalkingMatrix calculate(List<MatrixNode> nodes);

    boolean isWalkable(MatrixNode node);
}
