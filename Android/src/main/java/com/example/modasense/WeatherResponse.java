package com.example.modasense;

import java.util.List;

public class WeatherResponse {
    public List<Weather> weather;
    public Main main;

    public class Weather {
        public String main;
        public String description;
    }

    public class Main {
        public double temp;
        public double feels_like;
        public double temp_min;
        public double temp_max;
    }
}
