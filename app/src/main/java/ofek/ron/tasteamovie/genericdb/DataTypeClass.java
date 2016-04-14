package ofek.ron.tasteamovie.genericdb;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface DataTypeClass {
	public String defineKeys() default "";
}
