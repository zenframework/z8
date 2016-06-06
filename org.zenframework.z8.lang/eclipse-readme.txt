Eclipse Integration
-------------------------------
For BL projects add to .project file:

		<buildCommand>
			<name>org.zenframework.z8.pde.ProjectBuilder</name>
			<arguments>
				<dictionary>
					<key>JavaSource</key>
					<value>./.java</value>
				</dictionary>
			</arguments>
		</buildCommand>

and

		<nature>org.zenframework.z8.pde.ProjectNature</nature>
