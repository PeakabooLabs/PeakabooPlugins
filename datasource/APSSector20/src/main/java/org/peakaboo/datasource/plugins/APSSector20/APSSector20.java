package org.peakaboo.datasource.plugins.APSSector20;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

import org.peakaboo.dataset.io.DataInputAdapter;
import org.peakaboo.dataset.source.model.components.fileformat.FileFormat;
import org.peakaboo.dataset.source.model.components.physicalsize.PhysicalSize;
import org.peakaboo.dataset.source.model.components.physicalsize.SimplePhysicalSize;
import org.peakaboo.dataset.source.plugin.plugins.universalhdf5.FloatMatrixHDF5DataSource;
import org.peakaboo.dataset.source.plugin.plugins.universalhdf5.HDFReader;
import org.peakaboo.dataset.source.plugin.plugins.universalhdf5.SimpleHDF5FileFormat;
import org.peakaboo.framework.accent.Coord;
import org.peakaboo.framework.accent.log.OneLog;
import org.peakaboo.framework.cyclops.SISize;

public class APSSector20 extends FloatMatrixHDF5DataSource {

	//detector datasets are numbered: "/2D Scan/MCA 1", "/2D Scan/MCA 2", ...
	private static final String BASE = "/2D Scan/MCA ";
	private static final String FIRST = BASE + "1";
	private static final String NAME = "APS Sector 20";
	private static final String DESC = "HDF5 Data from Sector 20 of the APS Synchrotron";

	private SimplePhysicalSize physical;

	public APSSector20() {
		//axis order "yxz": dims are [row=height=y, column=width=x, channels=z]
		//data paths are discovered dynamically, so use the pathless constructor
		super("yxz", NAME, DESC);
	}

	@Override
	public String pluginVersion() {
		return "2.0";
	}

	@Override
	public String pluginUUID() {
		return "40cb5bfc-6ee3-4972-928f-f5bafd093e91";
	}

	@Override
	public FileFormat getFileFormat() {
		return new SimpleHDF5FileFormat(
				Arrays.asList(FIRST, "/2D Scan/X Positions", "/2D Scan/Y Positions"), NAME, DESC);
	}

	/**
	 * Discover the MCA detector datasets ("/2D Scan/MCA 1", "MCA 2", ...). The base
	 * class sums the returned data paths together, reproducing the old per-detector merge.
	 */
	@Override
	protected List<String> getDataPaths(List<DataInputAdapter> paths) {
		try (HDFReader reader = getReader(paths.get(0))) {
			List<String> dataPaths = new ArrayList<>();
			for (int n = 1; reader.exists(BASE + n); n++) {
				dataPaths.add(BASE + n);
			}
			return dataPaths;
		} catch (IOException e) {
			OneLog.log(Level.SEVERE, "Failed to detect MCA datasets", e);
			return List.of();
		}
	}

	@Override
	public Optional<PhysicalSize> getPhysicalSize() {
		return Optional.ofNullable(physical);
	}

	@Override
	protected void readMatrixMetadata(HDFReader reader, int channels) {
		int width = dataSize.getDataDimensions().x;
		int height = dataSize.getDataDimensions().y;

		//X Positions holds one value per point (flat index); Y Positions holds one value per row
		float[] xpos = reader.readFloatArray("/2D Scan/X Positions");
		float[] ypos = reader.readFloatArray("/2D Scan/Y Positions");

		physical = new SimplePhysicalSize(SISize.um);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int index = y * width + x;
				physical.putPoint(index, new Coord<>(xpos[index], ypos[y]));
			}
		}
	}

}
