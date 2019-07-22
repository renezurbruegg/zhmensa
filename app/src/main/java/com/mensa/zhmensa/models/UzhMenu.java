package com.mensa.zhmensa.models;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

import com.mensa.zhmensa.R;
import com.mensa.zhmensa.services.Helper;
import com.mensa.zhmensa.services.MensaManager;

import java.util.LinkedHashSet;
import java.util.Set;

public class UzhMenu extends Menu {

    private final Set<NutritionInfo> facts = new LinkedHashSet<>();

    public UzhMenu(String id, String name, String description, String prices, String allergene) {
        super(id, name, description, prices, allergene);
    }

    /*
    @Nullable
    @Override
    public String getDescription() {
        if(facts.size() != 0)
            return super.getDescription() + " \n" + facts.iterator().next().getHumanReadableInformation();
        return super.getDescription();
    } */

    //TODO
    @Nullable
    @Override
    public String getAllergene(Context ctx) {
        StringBuilder sb = new StringBuilder();
        sb.append(ctx.getString(R.string.nutrition_facts)).append("\n");

        for(NutritionInfo i : facts)
            sb.append(i.getHumanReadableInformation()).append("\n");

        sb.append("\n").append(super.getAllergene(ctx));
        return sb.toString();
    }

    @Nullable
    @Override
    public String getPrices() {
        return Helper.firstNonNull(super.getPrices(),"").replace("| CHF ","");
    }

    public void addNutritionFact(String name, String value) {
        Log.d("UZH. set nut fact", "name: " + name + " value: " + value);
        facts.add(new NutritionInfo(name, value));

    }

    private static class NutritionInfo {
        public String name;
        public String value;

        private String representation = null;
        public NutritionInfo(String name, String value) {
            this.name =  Helper.firstNonNull(name,"").replace("Energie", "Kalorien");
            this.value = value;
        }
        public String getHumanReadableInformation() {
            if(representation == null) {

                float val = 0;
                int max= 1;
                try {
                    if (name.contains("Kalorien") || name.contains("Calories")) {
                        String number = value.replaceAll("k?J.+", "").replace("'","").trim();
                        val = Float.valueOf(number);
                        max = 8700;

                    } else if (name.contains("Protein") || name.contains("Eiweiss")) {
                        val = Float.valueOf(value.replace("g", "").trim());
                        max = 50;

                    } else if (name.contains("Fat") || name.contains("Fett")) {
                        val = Float.valueOf(value.replace("g", "").trim());
                        max = 70;

                    } else if (name.contains("Carbohydrates") || name.contains("Kohlenhydrate")) {
                        val = Float.valueOf(value.replace("g", "").trim());
                        max = 310;

                    } else {
                        Log.e("UzhMenu.NutInfo", "unknown nutrition fact : " + name);
                    }
                } catch (NumberFormatException e) {
                    Log.e("e","e", e);
                }
                representation = name.replace(" ","") + ": " + value + " (" + (int) (100 * val / max)+ "%)";

            }
            return representation;
        }
    }

}
