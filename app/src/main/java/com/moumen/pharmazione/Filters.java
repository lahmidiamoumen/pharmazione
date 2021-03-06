/**
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 package com.moumen.pharmazione;

import android.text.TextUtils;

/**
 * Object for passing filters around.
 */
public class Filters {

    private String category = null;
    private String city = null;
    public Filters() {}

    public static Filters getDefault() {
        Filters filters = new Filters();
        return filters;
    }

    public boolean hasCategory() {
        return !(TextUtils.isEmpty(category));
    }

    public boolean hasCity() {
        return !(TextUtils.isEmpty(city));
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }




    public String getSearchDescription() {
        StringBuilder desc = new StringBuilder();

        if (category == null && city == null) {
            desc.append("<b>");
            desc.append("Tous les médicaments");
            desc.append("</b>");
        }

        if (category != null) {
            desc.append("<b>");
            desc.append(category);
            desc.append("</b>");
        }

        if (category != null && city != null) {
            desc.append(" • ");
//            desc.append(" dans ");
        }

        if (city != null) {
            desc.append("<b>");
            desc.append(city);
            desc.append("</b>");
        }

        return desc.toString();
    }

//    public String getOrderDescription(Context context) {
//        if (Restaurant.FIELD_PRICE.equals(sortBy)) {
//            return context.getString(R.string.sorted_by_price);
//        } else if (Restaurant.FIELD_POPULARITY.equals(sortBy)) {
//            return context.getString(R.string.sorted_by_popularity);
//        } else {
//            return context.getString(R.string.sorted_by_rating);
//        }
//    }
}
