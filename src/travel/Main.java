package travel;

import java.time.Clock;

import travel.data.SampleData;
import travel.service.TravelApp;
import travel.ui.ConsoleMenu;

/** Titik masuk aplikasi: merangkai TravelApp (logika) dengan ConsoleMenu (antarmuka) lalu menjalankannya. */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        TravelApp app = SampleData.createApp(Clock.systemDefaultZone());
        new ConsoleMenu(app, System.in, System.out).run();
    }
}
