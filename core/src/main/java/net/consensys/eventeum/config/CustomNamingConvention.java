package net.consensys.eventeum.config;


import io.micrometer.core.instrument.Meter.Type;
import io.micrometer.core.instrument.config.NamingConvention;
import io.micrometer.core.lang.Nullable;
import java.util.regex.Pattern;

public class CustomNamingConvention implements NamingConvention {
	private static final Pattern nameChars = Pattern.compile("[^a-zA-Z0-9_:]");
	private static final Pattern tagKeyChars = Pattern.compile("[^a-zA-Z0-9_]");
	private final String timerSuffix;
	private static final String prefix = "eventeum_";

	public CustomNamingConvention() {
		this("");
	}

	public CustomNamingConvention(String timerSuffix) {
		this.timerSuffix = timerSuffix;
	}

	public String name(String name, Type type, @Nullable String baseUnit) {
		String conventionName = prefix + NamingConvention.snakeCase.name(name, type, baseUnit);
		switch(type) {
			case COUNTER:
			case DISTRIBUTION_SUMMARY:
			case GAUGE:
				if (baseUnit != null && !conventionName.endsWith("_" + baseUnit)) {
					conventionName = conventionName + "_" + baseUnit;
				}
			default:
				switch(type) {
					case COUNTER:
						if (!conventionName.endsWith("_total")) {
							conventionName = conventionName + "_total";
						}
					case DISTRIBUTION_SUMMARY:
					case GAUGE:
					default:
						break;
					case TIMER:
					case LONG_TASK_TIMER:
						if (conventionName.endsWith(this.timerSuffix)) {
							conventionName = conventionName + "_seconds";
						} else if (!conventionName.endsWith("_seconds")) {
							conventionName = conventionName + this.timerSuffix + "_seconds";
						}
				}

				String sanitized = nameChars.matcher(conventionName).replaceAll("_");
				if (!Character.isLetter(sanitized.charAt(0))) {
					sanitized = "m_" + sanitized;
				}

				return sanitized;
		}
	}

	public String tagKey(String key) {
		String conventionKey = NamingConvention.snakeCase.tagKey(key);
		String sanitized = tagKeyChars.matcher(conventionKey).replaceAll("_");
		if (!Character.isLetter(sanitized.charAt(0))) {
			sanitized = "m_" + sanitized;
		}

		return sanitized;
	}
}
