package uk.ac.ed.inf.ilpcw1.service;

import org.springframework.stereotype.Service;
import uk.ac.ed.inf.ilpcw1.data.LngLat;

@Service
public class ValidationService {

    public boolean isValidLngLat(LngLat position) {
        if (position == null) {
            return false;
        }
        double lng = position.getLongitude();
        double lat = position.getLatitude();

        if (Double.isNaN(lng) || Double.isNaN(lat)) {
            return false;
        }

        return lng >= -180 && lng <= 180 && lat >= -90 && lat <= 90;
    }
}
