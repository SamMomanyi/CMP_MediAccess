@Composable
fun DashboardScreen(
    onNavigate: (Route) -> Unit,
    viewModel: DashboardViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val items = listOf(
                    Triple("Home", Icons.Default.Home, Route.Dashboard),
                    Triple("Records", Icons.AutoMirrored.Filled.List, Route.Records),
                    Triple("Hospitals", Icons.Default.Place, Route.Hospitals),
                    Triple("Profile", Icons.Default.Person, Route.Profile)
                )
                items.forEach { (label, icon, route) ->
                    NavigationBarItem(
                        selected = route == Route.Dashboard, // Simplified for now
                        onClick = { onNavigate(route) },
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label) }
                    )
                }
            }
        }
    ) { padding ->
        // Dashboard Content (Card with Medical ID and Generate Button)
        DashboardContent(
            padding = padding,
            user = state.user,
            onGenerateClick = { viewModel.onGenerateVisitCode() }
        )
    }
}