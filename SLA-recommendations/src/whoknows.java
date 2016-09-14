/**
 * Created by seaurchi on 9/14/16.
 */
import static spark.Spark.;

public class whoknows {
    public static void main(String[] args) {
        get("/hello", (req, res) -> "Hello World");
    }

}
