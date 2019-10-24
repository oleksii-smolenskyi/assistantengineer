package utils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Date;

/**
 * Клас для утилітних методів, які потрібні для будь-яких класів програми.
 *  @author Oleksii Smolenskyi
 */
public class UtilityMethods {
    /**
     * Використовується для визначення робочої теки програми, тобто з якої теки було здійснено запуск програми на виконання.
     * @return шлях до теки з якої запущено програму
     * @throws IOException при виникненні помилки отримання шляху до теки
     */
    public static String getMainPath() throws IOException {
        try {
            File path = new File(UtilityMethods.class.getProtectionDomain().getCodeSource().getLocation().toURI());
/*            if(!path.toString().contains("1_Lager\\TM_freigabe\\libs")) {
                System.exit(0);
                throw new IOException("Не можливо запустити програму!");
            }*/
            if (path.isFile())
                return path.getParentFile().getPath();
            else
                return path.getPath();
        } catch (URISyntaxException e) {
            throw new IOException(e.getMessage());
        }
    }

    // Convert OLE Automation Date to Date
    // https://docs.microsoft.com/en-us/dotnet/api/system.datetime.fromoadate?redirectedfrom=MSDN&view=netframework-4.8#System_DateTime_FromOADate_System_Double_
    public static Date fromDoubleToDateTime(double OADate)
    {
        long num = (long) ((OADate * 86400000.0) + ((OADate >= 0.0) ? 0.5 : -0.5));
        if (num < 0L) {
            num -= (num % 0x5265c00L) * 2L;
        }
        num += 0x3680b5e1fc00L;
        num -=  62135596800000L;

        return new Date(num);
    }
}
