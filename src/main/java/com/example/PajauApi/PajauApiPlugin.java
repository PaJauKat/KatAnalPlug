package com.example.PajauApi;

import lombok.NonNull;
import lombok.SneakyThrows;
import net.runelite.api.Client;
import net.runelite.api.CollisionDataFlag;
import net.runelite.api.SpriteID;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.RuneLite;
import net.runelite.client.plugins.Plugin;

import java.util.Objects;
import java.util.function.Predicate;

public class PajauApiPlugin extends Plugin {

    static Client client = RuneLite.getInjector().getInstance(Client.class);


    public static WorldPoint TilesAvalibleRadial(Client clt, int radio, WorldPoint ptCentral,Predicate<? super WorldPoint> predicado){

        int flagCondition = CollisionDataFlag.BLOCK_MOVEMENT_OBJECT + CollisionDataFlag.BLOCK_MOVEMENT_FLOOR +
                CollisionDataFlag.BLOCK_MOVEMENT_FLOOR_DECORATION;

        int[][] banderas = Objects.requireNonNull(clt.getCollisionMaps())[clt.getPlane()].getFlags();

        int x0 = ptCentral.getX() - clt.getBaseX();
        int y0 = ptCentral.getY() - clt.getBaseY();


        for (int i = 0; i <= 2*radio; i++) {
            if ((banderas[x0 + radio][y0 - radio + i] & flagCondition) == 0) {
                if ( predicado.test(ptCentral.dx(radio).dy(i-radio))) {
                    return ptCentral.dx(radio).dy(i-radio);
                }
            }
        }

        for (int i = 0; i <= 2 * radio; i++) {
            if ((banderas[x0 + radio - i][y0 + radio] & flagCondition) == 0) {
                if ( predicado.test(ptCentral.dx(radio-i).dy(radio))) {
                    return ptCentral.dx(radio-i).dy(radio);
                }
            }
        }

        for (int i = 0; i <= 2 * radio; i++) {
            if ((banderas[x0 - radio][y0 + radio - i] & flagCondition) == 0) {
                if ( predicado.test(ptCentral.dx(-radio).dy(radio-i))) {
                    return ptCentral.dx(-radio).dy(radio-i);
                }
            }
        }

        for (int i = 0; i <= 2 * radio; i++) {
            if ((banderas[x0 - radio + i][y0 - radio] & flagCondition) == 0) {
                if ( predicado.test(ptCentral.dx(i-radio).dy(-radio))) {
                    return ptCentral.dx(i-radio).dy(-radio);
                }
            }
        }

        return null;

    }


}
