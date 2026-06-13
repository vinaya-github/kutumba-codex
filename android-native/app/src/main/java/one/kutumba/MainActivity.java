package one.kutumba;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends Activity {
  private static final String STORAGE = "kutumba_native_state";
  private static final String STATE_KEY = "state";

  private static final int BG = Color.rgb(244, 241, 234);
  private static final int SURFACE = Color.rgb(255, 253, 248);
  private static final int CARD = Color.WHITE;
  private static final int INK = Color.rgb(37, 37, 37);
  private static final int MUTED = Color.rgb(108, 102, 93);
  private static final int LINE = Color.rgb(222, 216, 205);
  private static final int ACCENT = Color.rgb(31, 122, 107);
  private static final int ACCENT_DARK = Color.rgb(21, 89, 79);
  private static final int ACCENT_SOFT = Color.rgb(216, 238, 232);
  private static final int GOLD = Color.rgb(184, 138, 53);
  private static final int ROSE_SOFT = Color.rgb(247, 223, 226);
  private static final int ROSE = Color.rgb(126, 40, 52);

  private final List<String> categories = Arrays.asList(
      "Money",
      "Protection",
      "Property",
      "Debt",
      "Identity and tax",
      "Digital life",
      "People to contact",
      "Instructions"
  );

  private LinearLayout root;
  private TextView titleView;
  private Button lockButton;
  private FrameLayout contentHost;
  private LinearLayout navBar;
  private final List<Button> navButtons = new ArrayList<>();

  private AppState state;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Window window = getWindow();
    window.setStatusBarColor(SURFACE);
    window.setNavigationBarColor(SURFACE);
    state = loadState();
    buildShell();
    render();
  }

  private void buildShell() {
    root = new LinearLayout(this);
    root.setOrientation(LinearLayout.VERTICAL);
    root.setBackgroundColor(SURFACE);
    root.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));

    LinearLayout topBar = new LinearLayout(this);
    topBar.setOrientation(LinearLayout.HORIZONTAL);
    topBar.setGravity(Gravity.CENTER_VERTICAL);
    topBar.setPadding(dp(18), dp(14), dp(18), dp(10));
    topBar.setBackgroundColor(SURFACE);
    root.addView(topBar, new LinearLayout.LayoutParams(-1, dp(78)));

    LinearLayout titleStack = new LinearLayout(this);
    titleStack.setOrientation(LinearLayout.VERTICAL);
    topBar.addView(titleStack, new LinearLayout.LayoutParams(0, -1, 1));

    TextView brand = label("Kutumba");
    titleStack.addView(brand);

    titleView = text("Household", 23, INK, Typeface.BOLD);
    titleStack.addView(titleView);

    lockButton = smallIconButton("Lock");
    lockButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        state.unlocked = !state.unlocked;
        saveState();
        render();
      }
    });
    topBar.addView(lockButton, new LinearLayout.LayoutParams(dp(62), dp(42)));

    contentHost = new FrameLayout(this);
    contentHost.setBackgroundColor(SURFACE);
    root.addView(contentHost, new LinearLayout.LayoutParams(-1, 0, 1));

    navBar = new LinearLayout(this);
    navBar.setOrientation(LinearLayout.HORIZONTAL);
    navBar.setPadding(dp(8), dp(8), dp(8), dp(8));
    navBar.setBackgroundColor(SURFACE);
    root.addView(navBar, new LinearLayout.LayoutParams(-1, dp(78)));

    addNavButton("Home");
    addNavButton("Records");
    addNavButton("People");
    addNavButton("Vault");

    setContentView(root);
  }

  private void addNavButton(final String viewName) {
    Button button = new Button(this);
    button.setAllCaps(false);
    button.setText(viewName);
    button.setTextSize(12);
    button.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
    button.setTextColor(MUTED);
    button.setBackground(rounded(Color.TRANSPARENT, 8, Color.TRANSPARENT));
    button.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        state.currentView = viewName;
        saveState();
        render();
      }
    });
    navButtons.add(button);
    navBar.addView(button, new LinearLayout.LayoutParams(0, -1, 1));
  }

  private void render() {
    contentHost.removeAllViews();
    titleView.setText(state.currentView.equals("Home") ? "Household" : state.currentView);
    lockButton.setText(state.unlocked ? "Lock" : "Unlock");
    updateNav();

    if (!state.unlocked) {
      renderLocked();
      return;
    }

    if ("Records".equals(state.currentView)) {
      renderRecords();
    } else if ("People".equals(state.currentView)) {
      renderPeople();
    } else if ("Vault".equals(state.currentView)) {
      renderVault();
    } else {
      renderHome();
    }
  }

  private void updateNav() {
    for (Button button : navButtons) {
      boolean selected = button.getText().toString().equals(state.currentView);
      button.setTextColor(selected ? ACCENT_DARK : MUTED);
      button.setBackground(selected
          ? rounded(ACCENT_SOFT, 8, ACCENT_SOFT)
          : rounded(Color.TRANSPARENT, 8, Color.TRANSPARENT));
    }
  }

  private ScrollView screen() {
    ScrollView scrollView = new ScrollView(this);
    scrollView.setFillViewport(false);
    scrollView.setBackgroundColor(SURFACE);
    LinearLayout content = new LinearLayout(this);
    content.setOrientation(LinearLayout.VERTICAL);
    content.setPadding(dp(16), dp(14), dp(16), dp(18));
    scrollView.addView(content, new ScrollView.LayoutParams(-1, -2));
    scrollView.setTag(content);
    contentHost.addView(scrollView, new FrameLayout.LayoutParams(-1, -1));
    return scrollView;
  }

  private LinearLayout content(ScrollView scrollView) {
    return (LinearLayout) scrollView.getTag();
  }

  private void renderLocked() {
    ScrollView scrollView = screen();
    LinearLayout content = content(scrollView);
    LinearLayout panel = card();
    panel.setGravity(Gravity.CENTER_HORIZONTAL);
    panel.setPadding(dp(22), dp(26), dp(22), dp(26));
    TextView mark = badge("PRIVATE", ACCENT_DARK, ACCENT_SOFT);
    panel.addView(mark);
    TextView heading = text("Private household space", 22, INK, Typeface.BOLD);
    heading.setGravity(Gravity.CENTER);
    addWithTop(panel, heading, 14);
    TextView body = paragraph("Kutumba opens after a local unlock in this first native build.");
    body.setGravity(Gravity.CENTER);
    addWithTop(panel, body, 8);
    Button unlock = primaryButton("Unlock");
    unlock.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        state.unlocked = true;
        saveState();
        render();
      }
    });
    addWithTop(panel, unlock, 18);
    content.addView(panel);
  }

  private void renderHome() {
    ScrollView scrollView = screen();
    LinearLayout content = content(scrollView);
    int score = readiness();

    LinearLayout hero = new LinearLayout(this);
    hero.setOrientation(LinearLayout.VERTICAL);
    hero.setPadding(dp(18), dp(18), dp(18), dp(18));
    hero.setBackground(rounded(ACCENT_DARK, 8, ACCENT_DARK));
    content.addView(hero, new LinearLayout.LayoutParams(-1, -2));

    TextView heroLabel = label("Continuity readiness");
    heroLabel.setTextColor(Color.rgb(225, 241, 236));
    hero.addView(heroLabel);

    LinearLayout scoreRow = new LinearLayout(this);
    scoreRow.setGravity(Gravity.CENTER_VERTICAL);
    addWithTop(hero, scoreRow, 8);

    TextView scoreText = text(String.valueOf(score), 36, Color.WHITE, Typeface.BOLD);
    scoreText.setGravity(Gravity.CENTER);
    scoreText.setBackground(rounded(GOLD, 8, GOLD));
    scoreRow.addView(scoreText, new LinearLayout.LayoutParams(dp(82), dp(70)));

    LinearLayout scoreCopy = new LinearLayout(this);
    scoreCopy.setOrientation(LinearLayout.VERTICAL);
    LinearLayout.LayoutParams copyParams = new LinearLayout.LayoutParams(0, -2, 1);
    copyParams.leftMargin = dp(14);
    scoreRow.addView(scoreCopy, copyParams);
    scoreCopy.addView(text(readinessTitle(score), 22, Color.WHITE, Typeface.BOLD));
    TextView summary = paragraph(homeSummary(score));
    summary.setTextColor(Color.rgb(225, 241, 236));
    addWithTop(scoreCopy, summary, 6);

    ProgressBar progress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
    progress.setMax(100);
    progress.setProgress(score);
    addWithTop(hero, progress, 14);

    LinearLayout actions = new LinearLayout(this);
    actions.setOrientation(LinearLayout.HORIZONTAL);
    addWithTop(content, actions, 14);
    actions.addView(actionButton("Add record", new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        showAddRecordDialog();
      }
    }), new LinearLayout.LayoutParams(0, dp(72), 1));
    addSpace(actions, 10, 0);
    actions.addView(actionButton("Steward", new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        showStewardDialog();
      }
    }), new LinearLayout.LayoutParams(0, dp(72), 1));
    addSpace(actions, 10, 0);
    actions.addView(actionButton("Emergency", new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        showEmergencyDialog();
      }
    }), new LinearLayout.LayoutParams(0, dp(72), 1));

    LinearLayout checklist = section("Checklist", completedTasks() + "/" + state.tasks.size() + " ready");
    for (final Task task : state.tasks) {
      checklist.addView(taskRow(task));
    }
    addWithTop(content, checklist, 14);

    LinearLayout access = section("Access map", "Who can see what");
    for (Person person : state.people) {
      access.addView(personMini(person));
    }
    addWithTop(content, access, 14);

    LinearLayout recent = section("Recent records", state.records.isEmpty() ? "No records yet" : "Continuity map");
    if (state.records.isEmpty()) {
      recent.addView(emptyBlock("Start with one useful reference",
          "Add a record that would help the family know what exists and what to do next."));
    } else {
      int count = Math.min(3, state.records.size());
      for (int i = 0; i < count; i++) {
        recent.addView(recordCard(state.records.get(i)));
      }
    }
    addWithTop(content, recent, 14);
  }

  private void renderRecords() {
    ScrollView scrollView = screen();
    LinearLayout content = content(scrollView);

    LinearLayout top = section("Continuity records", filteredRecords().size() + " shown");
    Button add = primaryButton("Add record");
    add.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        showAddRecordDialog();
      }
    });
    top.addView(add);

    HorizontalScrollView horizontalScrollView = new HorizontalScrollView(this);
    horizontalScrollView.setHorizontalScrollBarEnabled(false);
    LinearLayout filters = new LinearLayout(this);
    filters.setOrientation(LinearLayout.HORIZONTAL);
    horizontalScrollView.addView(filters);
    addFilterButton(filters, "All");
    for (String category : categories) {
      addFilterButton(filters, category);
    }
    addWithTop(top, horizontalScrollView, 12);
    content.addView(top);

    List<Record> records = filteredRecords();
    if (records.isEmpty()) {
      addWithTop(content, emptyBlock("Start with one useful reference",
          "Use records for accounts, policies, property, contacts, instructions, and document bundles."), 14);
    } else {
      for (Record record : records) {
        addWithTop(content, recordCard(record), 10);
      }
    }
  }

  private void renderPeople() {
    ScrollView scrollView = screen();
    LinearLayout content = content(scrollView);
    LinearLayout header = section("People", "Household access");
    Button edit = secondaryButton("Edit steward");
    edit.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        showStewardDialog();
      }
    });
    header.addView(edit);
    content.addView(header);

    for (Person person : state.people) {
      addWithTop(content, personCard(person), 12);
    }
  }

  private void renderVault() {
    ScrollView scrollView = screen();
    LinearLayout content = content(scrollView);
    LinearLayout header = section("Vault", state.documents.size() + " document references");
    Button add = primaryButton("Add document");
    add.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        showDocumentDialog();
      }
    });
    header.addView(add);
    content.addView(header);

    for (DocumentRef document : state.documents) {
      addWithTop(content, documentCard(document), 10);
    }
  }

  private LinearLayout section(String eyebrow, String heading) {
    LinearLayout section = card();
    section.addView(label(eyebrow));
    addWithTop(section, text(heading, 22, INK, Typeface.BOLD), 2);
    return section;
  }

  private View taskRow(final Task task) {
    LinearLayout row = new LinearLayout(this);
    row.setGravity(Gravity.CENTER_VERTICAL);
    row.setPadding(dp(10), dp(10), dp(10), dp(10));
    row.setBackground(rounded(Color.rgb(251, 248, 241), 8, Color.rgb(235, 228, 215)));
    LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(-1, -2);
    rowParams.topMargin = dp(10);
    row.setLayoutParams(rowParams);

    Button check = new Button(this);
    check.setAllCaps(false);
    check.setText(task.done ? "OK" : "");
    check.setTextSize(18);
    check.setTextColor(Color.WHITE);
    check.setBackground(rounded(task.done ? ACCENT : Color.WHITE, 8, task.done ? ACCENT : LINE));
    check.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        task.done = !task.done;
        saveState();
        render();
      }
    });
    row.addView(check, new LinearLayout.LayoutParams(dp(34), dp(34)));

    LinearLayout textStack = new LinearLayout(this);
    textStack.setOrientation(LinearLayout.VERTICAL);
    LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(0, -2, 1);
    textParams.leftMargin = dp(10);
    row.addView(textStack, textParams);
    textStack.addView(text(task.title, 16, INK, Typeface.BOLD));
    addWithTop(textStack, paragraph(task.detail), 3);
    return row;
  }

  private View personMini(Person person) {
    LinearLayout row = new LinearLayout(this);
    row.setGravity(Gravity.CENTER_VERTICAL);
    row.setPadding(dp(10), dp(10), dp(10), dp(10));
    row.setBackground(rounded(Color.rgb(251, 248, 241), 8, Color.rgb(235, 228, 215)));
    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
    params.topMargin = dp(10);
    row.setLayoutParams(params);

    row.addView(avatar(initials(displayName(person)), dp(44), ACCENT_DARK));

    LinearLayout stack = new LinearLayout(this);
    stack.setOrientation(LinearLayout.VERTICAL);
    LinearLayout.LayoutParams stackParams = new LinearLayout.LayoutParams(0, -2, 1);
    stackParams.leftMargin = dp(10);
    row.addView(stack, stackParams);
    stack.addView(text(displayName(person), 16, INK, Typeface.BOLD));
    addWithTop(stack, paragraph(roleLabel(person.role) + " - " + visibleRecords(person).size() + " visible"), 2);
    return row;
  }

  private View personCard(Person person) {
    LinearLayout card = card();
    LinearLayout top = new LinearLayout(this);
    top.setGravity(Gravity.CENTER_VERTICAL);
    card.addView(top);
    top.addView(avatar(initials(displayName(person)), dp(56), person.role.equals("steward") ? ACCENT_DARK : Color.rgb(56, 95, 131)));

    LinearLayout stack = new LinearLayout(this);
    stack.setOrientation(LinearLayout.VERTICAL);
    LinearLayout.LayoutParams stackParams = new LinearLayout.LayoutParams(0, -2, 1);
    stackParams.leftMargin = dp(12);
    top.addView(stack, stackParams);
    stack.addView(label(emptyTo(person.relationship, "Not selected")));
    addWithTop(stack, text(displayName(person), 21, INK, Typeface.BOLD), 2);
    addWithTop(stack, paragraph(roleLabel(person.role) + " - " + inviteLabel(person.inviteStatus)), 3);

    LinearLayout metrics = new LinearLayout(this);
    metrics.setOrientation(LinearLayout.HORIZONTAL);
    addWithTop(card, metrics, 14);
    metrics.addView(metric(String.valueOf(visibleRecords(person).size()), "Visible records"), new LinearLayout.LayoutParams(0, -2, 1));
    addSpace(metrics, 10, 0);
    metrics.addView(metric(person.lastReviewed, "Review"), new LinearLayout.LayoutParams(0, -2, 1));

    if (person.role.equals("steward")) {
      Button button = secondaryButton("Set steward details");
      button.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          showStewardDialog();
        }
      });
      addWithTop(card, button, 14);
    }
    return card;
  }

  private View recordCard(final Record record) {
    LinearLayout card = new LinearLayout(this);
    card.setOrientation(LinearLayout.HORIZONTAL);
    card.setGravity(Gravity.CENTER_VERTICAL);
    card.setPadding(dp(12), dp(12), dp(8), dp(12));
    card.setBackground(rounded(CARD, 8, LINE));

    TextView icon = avatar(categoryInitial(record.category), dp(44), ACCENT_SOFT);
    icon.setTextColor(ACCENT_DARK);
    card.addView(icon);

    LinearLayout stack = new LinearLayout(this);
    stack.setOrientation(LinearLayout.VERTICAL);
    LinearLayout.LayoutParams stackParams = new LinearLayout.LayoutParams(0, -2, 1);
    stackParams.leftMargin = dp(12);
    card.addView(stack, stackParams);
    stack.addView(label(record.category));
    addWithTop(stack, text(record.title, 18, INK, Typeface.BOLD), 2);
    addWithTop(stack, paragraph(record.note.length() == 0 ? "No note added" : record.note), 3);

    LinearLayout pills = new LinearLayout(this);
    pills.setOrientation(LinearLayout.HORIZONTAL);
    addWithTop(stack, pills, 9);
    pills.addView(badge(personName(record.ownerId).split(" ")[0], MUTED, Color.rgb(241, 234, 220)));
    addSpace(pills, 6, 0);
    pills.addView(badge(visibilityNames(record), record.visibility.contains("steward") ? ACCENT_DARK : MUTED,
        record.visibility.contains("steward") ? ACCENT_SOFT : Color.rgb(241, 234, 220)));

    Button more = smallIconButton(record.reviewed ? "Done" : "Open");
    more.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        showRecordDetailDialog(record);
      }
    });
    card.addView(more, new LinearLayout.LayoutParams(dp(68), dp(42)));
    return card;
  }

  private View documentCard(DocumentRef document) {
    LinearLayout card = new LinearLayout(this);
    card.setGravity(Gravity.CENTER_VERTICAL);
    card.setPadding(dp(12), dp(12), dp(12), dp(12));
    card.setBackground(rounded(CARD, 8, LINE));

    TextView icon = avatar("DOC", dp(44), ACCENT_SOFT);
    icon.setTextSize(11);
    icon.setTextColor(ACCENT_DARK);
    card.addView(icon);

    LinearLayout stack = new LinearLayout(this);
    stack.setOrientation(LinearLayout.VERTICAL);
    LinearLayout.LayoutParams stackParams = new LinearLayout.LayoutParams(0, -2, 1);
    stackParams.leftMargin = dp(12);
    card.addView(stack, stackParams);
    stack.addView(label(document.type));
    addWithTop(stack, text(document.title, 17, INK, Typeface.BOLD), 2);
    addWithTop(stack, paragraph("Linked to " + document.linkedTo), 3);

    card.addView(badge(document.status, MUTED, Color.rgb(241, 234, 220)));
    return card;
  }

  private View emptyBlock(String heading, String body) {
    LinearLayout empty = new LinearLayout(this);
    empty.setOrientation(LinearLayout.VERTICAL);
    empty.setPadding(dp(18), dp(18), dp(18), dp(18));
    empty.setBackground(rounded(Color.rgb(251, 248, 241), 8, LINE));
    empty.addView(text(heading, 20, INK, Typeface.BOLD));
    addWithTop(empty, paragraph(body), 7);
    Button add = primaryButton("Add record");
    add.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        showAddRecordDialog();
      }
    });
    addWithTop(empty, add, 14);
    return empty;
  }

  private void addFilterButton(LinearLayout parent, final String value) {
    Button button = secondaryButton(value);
    boolean selected = state.selectedCategory.equals(value);
    button.setTextColor(selected ? Color.WHITE : ACCENT_DARK);
    button.setBackground(selected ? rounded(INK, 24, INK) : rounded(ACCENT_SOFT, 24, ACCENT_SOFT));
    button.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        state.selectedCategory = value;
        saveState();
        render();
      }
    });
    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-2, dp(40));
    params.rightMargin = dp(8);
    parent.addView(button, params);
  }

  private void showAddRecordDialog() {
    final LinearLayout form = dialogForm();
    final EditText title = input("Example: Term insurance policy");
    final Spinner category = spinner(categories);
    final Spinner owner = spinner(personNames(false));
    final CheckBox vinaya = checkbox("Vinaya", true);
    final CheckBox divya = checkbox("Divya", true);
    final CheckBox stewardAccess = checkbox("Trusted steward", false);
    final EditText note = multiline("What should the family know?");
    final EditText instruction = multiline("What should happen if the owner is unavailable?");

    form.addView(field("Title", title));
    form.addView(field("Life area", category));
    form.addView(field("Owner", owner));
    form.addView(field("Visible to", vinaya, divya, stewardAccess));
    form.addView(field("Helpful note", note));
    form.addView(field("Emergency instruction", instruction));

    final AlertDialog dialog = new AlertDialog.Builder(this)
        .setTitle("New continuity record")
        .setView(wrapDialog(form))
        .setNegativeButton("Cancel", null)
        .setPositiveButton("Save", null)
        .create();
    dialog.setOnShowListener(new android.content.DialogInterface.OnShowListener() {
      @Override
      public void onShow(android.content.DialogInterface dialogInterface) {
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            String titleText = title.getText().toString().trim();
            if (titleText.length() == 0) {
              title.setError("Required");
              return;
            }
            Record record = new Record();
            record.id = "record_" + UUID.randomUUID().toString();
            record.title = titleText;
            record.category = category.getSelectedItem().toString();
            record.ownerId = personIdForName(owner.getSelectedItem().toString());
            record.note = note.getText().toString().trim();
            record.instruction = instruction.getText().toString().trim();
            if (vinaya.isChecked()) record.visibility.add("person_vinaya");
            if (divya.isChecked()) record.visibility.add("person_divya");
            if (stewardAccess.isChecked()) record.visibility.add("steward");
            if (record.visibility.isEmpty()) {
              record.visibility.add(record.ownerId);
            }
            state.records.add(0, record);
            markTaskDone("task_add_first_record");
            state.currentView = "Records";
            saveState();
            dialog.dismiss();
            render();
          }
        });
      }
    });
    dialog.show();
  }

  private void showStewardDialog() {
    final Person current = steward();
    final LinearLayout form = dialogForm();
    final EditText name = input("Trusted family member");
    name.setText(current.name);
    final EditText relationship = input("Example: Brother, sister, son");
    relationship.setText(current.relationship);
    final EditText age = input("Optional");
    age.setText(current.age);
    form.addView(field("Name", name));
    form.addView(field("Relationship", relationship));
    form.addView(field("Age", age));
    form.addView(paragraph("The steward sees nothing by default. Share records explicitly from each record."));

    final AlertDialog dialog = new AlertDialog.Builder(this)
        .setTitle("Trusted steward")
        .setView(wrapDialog(form))
        .setNegativeButton("Cancel", null)
        .setPositiveButton("Save", null)
        .create();
    dialog.setOnShowListener(new android.content.DialogInterface.OnShowListener() {
      @Override
      public void onShow(android.content.DialogInterface dialogInterface) {
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            current.name = name.getText().toString().trim();
            current.relationship = relationship.getText().toString().trim();
            current.age = age.getText().toString().trim();
            current.inviteStatus = current.name.length() == 0 ? "not_selected" : "draft_invite";
            current.lastReviewed = "Today";
            if (current.name.length() > 0) {
              markTaskDone("task_choose_steward");
            }
            saveState();
            dialog.dismiss();
            render();
          }
        });
      }
    });
    dialog.show();
  }

  private void showDocumentDialog() {
    final LinearLayout form = dialogForm();
    final EditText title = input("Example: Home loan sanction letter");
    final Spinner type = spinner(Arrays.asList("PDF", "Photo", "Folder", "Reference"));
    final EditText linkedTo = input("Household, record, or person");
    form.addView(field("Document name", title));
    form.addView(field("Type", type));
    form.addView(field("Linked to", linkedTo));

    final AlertDialog dialog = new AlertDialog.Builder(this)
        .setTitle("Vault reference")
        .setView(wrapDialog(form))
        .setNegativeButton("Cancel", null)
        .setPositiveButton("Save", null)
        .create();
    dialog.setOnShowListener(new android.content.DialogInterface.OnShowListener() {
      @Override
      public void onShow(android.content.DialogInterface dialogInterface) {
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            String titleText = title.getText().toString().trim();
            if (titleText.length() == 0) {
              title.setError("Required");
              return;
            }
            DocumentRef document = new DocumentRef();
            document.id = "doc_" + UUID.randomUUID().toString();
            document.title = titleText;
            document.type = type.getSelectedItem().toString();
            document.linkedTo = linkedTo.getText().toString().trim().length() == 0
                ? "Household"
                : linkedTo.getText().toString().trim();
            document.status = "placeholder";
            state.documents.add(0, document);
            saveState();
            dialog.dismiss();
            render();
          }
        });
      }
    });
    dialog.show();
  }

  private void showEmergencyDialog() {
    markTaskDone("task_preview_emergency");
    saveState();
    LinearLayout body = dialogForm();
    TextView explainer = paragraph("Only records explicitly shared with the steward appear here.");
    body.addView(explainer);
    List<Record> records = emergencyRecords();
    if (records.isEmpty()) {
      addWithTop(body, emptyMessage("No emergency records yet", "Share a record with the steward to make it visible here."), 12);
    } else {
      for (Record record : records) {
        LinearLayout item = card();
        item.addView(label(record.category));
        addWithTop(item, text(record.title, 18, INK, Typeface.BOLD), 2);
        addWithTop(item, paragraph(record.instruction.length() == 0 ? record.note : record.instruction), 5);
        addWithTop(item, badge("Owner: " + personName(record.ownerId), MUTED, Color.rgb(241, 234, 220)), 10);
        addWithTop(body, item, 10);
      }
    }
    new AlertDialog.Builder(this)
        .setTitle("Emergency preview")
        .setView(wrapDialog(body))
        .setPositiveButton("Done", null)
        .show();
    render();
  }

  private void showRecordDetailDialog(final Record record) {
    LinearLayout body = dialogForm();
    body.addView(detailRow("Owner", personName(record.ownerId)));
    body.addView(detailRow("Visible to", visibilityNames(record)));
    body.addView(detailRow("Review", record.reviewed ? "Reviewed" : "Needs review"));
    addWithTop(body, label("Helpful note"), 8);
    body.addView(paragraph(record.note.length() == 0 ? "No note added." : record.note));
    addWithTop(body, label("Emergency instruction"), 10);
    body.addView(paragraph(record.instruction.length() == 0 ? "No emergency instruction added." : record.instruction));

    LinearLayout actions = new LinearLayout(this);
    actions.setOrientation(LinearLayout.HORIZONTAL);
    Button visibility = secondaryButton("Visibility");
    Button review = primaryButton(record.reviewed ? "Mark unreviewed" : "Mark reviewed");
    actions.addView(visibility, new LinearLayout.LayoutParams(0, dp(44), 1));
    addSpace(actions, 10, 0);
    actions.addView(review, new LinearLayout.LayoutParams(0, dp(44), 1));
    addWithTop(body, actions, 14);

    final AlertDialog dialog = new AlertDialog.Builder(this)
        .setTitle(record.title)
        .setView(wrapDialog(body))
        .setNegativeButton("Delete", null)
        .setPositiveButton("Close", null)
        .create();

    dialog.setOnShowListener(new android.content.DialogInterface.OnShowListener() {
      @Override
      public void onShow(android.content.DialogInterface dialogInterface) {
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ROSE);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            state.records.remove(record);
            saveState();
            dialog.dismiss();
            render();
          }
        });
      }
    });

    visibility.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        dialog.dismiss();
        showVisibilityDialog(record);
      }
    });
    review.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        record.reviewed = !record.reviewed;
        saveState();
        dialog.dismiss();
        render();
      }
    });
    dialog.show();
  }

  private void showVisibilityDialog(final Record record) {
    final LinearLayout form = dialogForm();
    final CheckBox vinaya = checkbox("Vinaya", record.visibility.contains("person_vinaya"));
    final CheckBox divya = checkbox("Divya", record.visibility.contains("person_divya"));
    final CheckBox stewardAccess = checkbox("Trusted steward", record.visibility.contains("steward"));
    form.addView(field("Visible to", vinaya, divya, stewardAccess));
    form.addView(paragraph("Steward access should be used only for emergency-useful information."));

    final AlertDialog dialog = new AlertDialog.Builder(this)
        .setTitle("Visibility")
        .setView(wrapDialog(form))
        .setNegativeButton("Cancel", null)
        .setPositiveButton("Save", null)
        .create();
    dialog.setOnShowListener(new android.content.DialogInterface.OnShowListener() {
      @Override
      public void onShow(android.content.DialogInterface dialogInterface) {
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            record.visibility.clear();
            if (vinaya.isChecked()) record.visibility.add("person_vinaya");
            if (divya.isChecked()) record.visibility.add("person_divya");
            if (stewardAccess.isChecked()) record.visibility.add("steward");
            if (record.visibility.isEmpty()) record.visibility.add(record.ownerId);
            saveState();
            dialog.dismiss();
            render();
          }
        });
      }
    });
    dialog.show();
  }

  private AppState loadState() {
    SharedPreferences prefs = getSharedPreferences(STORAGE, MODE_PRIVATE);
    String raw = prefs.getString(STATE_KEY, null);
    if (raw == null) return AppState.initial();
    try {
      return AppState.fromJson(new JSONObject(raw));
    } catch (JSONException exception) {
      return AppState.initial();
    }
  }

  private void saveState() {
    getSharedPreferences(STORAGE, MODE_PRIVATE)
        .edit()
        .putString(STATE_KEY, state.toJson().toString())
        .apply();
  }

  private int readiness() {
    boolean hasSteward = steward().name.length() > 0;
    int recordCount = state.records.size();
    int emergencyCount = emergencyRecords().size();
    int reviewed = 0;
    for (Record record : state.records) {
      if (record.reviewed) reviewed++;
    }
    int score = 24;
    if (hasSteward) score += 18;
    if (recordCount > 0) score += 16;
    if (recordCount >= 3) score += 10;
    if (emergencyCount > 0) score += 14;
    if (reviewed == recordCount && recordCount > 0) score += 8;
    return Math.min(score, 100);
  }

  private String readinessTitle(int score) {
    if (score < 50) return "Getting started";
    if (score < 80) return "Taking shape";
    return "Well prepared";
  }

  private String homeSummary(int score) {
    if (steward().name.length() == 0) {
      return "Choose a trusted family steward, then share only the records they should see.";
    }
    if (emergencyRecords().isEmpty()) {
      return "Your steward is named, but no emergency-visible records have been selected.";
    }
    if (score < 80) {
      return "The household map is forming. Add notes and review states to make it dependable.";
    }
    return "The household has a clear continuity path with visible access decisions.";
  }

  private int completedTasks() {
    int count = 0;
    for (Task task : state.tasks) {
      if (task.done) count++;
    }
    return count;
  }

  private void markTaskDone(String id) {
    for (Task task : state.tasks) {
      if (task.id.equals(id)) {
        task.done = true;
      }
    }
  }

  private Person steward() {
    for (Person person : state.people) {
      if (person.role.equals("steward")) return person;
    }
    return state.people.get(2);
  }

  private List<Record> emergencyRecords() {
    List<Record> records = new ArrayList<>();
    for (Record record : state.records) {
      if (record.visibility.contains("steward")) {
        records.add(record);
      }
    }
    return records;
  }

  private List<Record> filteredRecords() {
    if ("All".equals(state.selectedCategory)) return state.records;
    List<Record> records = new ArrayList<>();
    for (Record record : state.records) {
      if (state.selectedCategory.equals(record.category)) records.add(record);
    }
    return records;
  }

  private List<Record> visibleRecords(Person person) {
    List<Record> records = new ArrayList<>();
    for (Record record : state.records) {
      if (person.role.equals("steward")) {
        if (record.visibility.contains("steward")) records.add(record);
      } else if (record.visibility.contains(person.id)) {
        records.add(record);
      }
    }
    return records;
  }

  private String visibilityNames(Record record) {
    List<String> names = new ArrayList<>();
    for (String value : record.visibility) {
      if ("steward".equals(value)) {
        names.add(steward().name.length() == 0 ? "Steward" : steward().name.split(" ")[0]);
      } else {
        names.add(personName(value).split(" ")[0]);
      }
    }
    return join(names, ", ");
  }

  private String personName(String id) {
    for (Person person : state.people) {
      if (person.id.equals(id)) return displayName(person);
    }
    return "Unassigned";
  }

  private String personIdForName(String name) {
    for (Person person : state.people) {
      if (person.name.equals(name)) return person.id;
    }
    return "person_vinaya";
  }

  private List<String> personNames(boolean includeSteward) {
    List<String> names = new ArrayList<>();
    for (Person person : state.people) {
      if (includeSteward || !person.role.equals("steward")) names.add(person.name);
    }
    return names;
  }

  private String displayName(Person person) {
    if (person.name.length() > 0) return person.name;
    return "Trusted family steward";
  }

  private String roleLabel(String role) {
    if ("owner".equals(role)) return "Owner";
    if ("co_owner".equals(role)) return "Co-owner";
    return "Steward";
  }

  private String inviteLabel(String status) {
    if ("active".equals(status)) return "Active";
    if ("draft_invite".equals(status)) return "Ready to invite";
    return "Not selected";
  }

  private String initials(String name) {
    String[] parts = name.trim().split("\\s+");
    String value = "";
    for (int i = 0; i < parts.length && i < 2; i++) {
      if (parts[i].length() > 0) value += parts[i].substring(0, 1).toUpperCase();
    }
    return value.length() == 0 ? "K" : value;
  }

  private String categoryInitial(String category) {
    String[] parts = category.split("\\s+");
    String value = "";
    for (int i = 0; i < parts.length && i < 2; i++) {
      value += parts[i].substring(0, 1).toUpperCase();
    }
    return value;
  }

  private String join(List<String> values, String separator) {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < values.size(); i++) {
      if (i > 0) builder.append(separator);
      builder.append(values.get(i));
    }
    return builder.toString();
  }

  private String emptyTo(String value, String fallback) {
    return value == null || value.length() == 0 ? fallback : value;
  }

  private int dp(int value) {
    return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
  }

  private TextView text(String value, int size, int color, int style) {
    TextView view = new TextView(this);
    view.setText(value);
    view.setTextSize(size);
    view.setTextColor(color);
    view.setTypeface(Typeface.DEFAULT, style);
    view.setIncludeFontPadding(true);
    return view;
  }

  private TextView paragraph(String value) {
    TextView view = text(value, 14, MUTED, Typeface.NORMAL);
    view.setLineSpacing(0, 1.08f);
    return view;
  }

  private TextView label(String value) {
    TextView view = text(value.toUpperCase(), 11, MUTED, Typeface.BOLD);
    return view;
  }

  private TextView badge(String value, int color, int bg) {
    TextView view = text(value, 12, color, Typeface.BOLD);
    view.setGravity(Gravity.CENTER);
    view.setPadding(dp(9), dp(5), dp(9), dp(5));
    view.setBackground(rounded(bg, 24, bg));
    return view;
  }

  private TextView avatar(String value, int size, int bg) {
    TextView view = text(value, 14, Color.WHITE, Typeface.BOLD);
    view.setGravity(Gravity.CENTER);
    view.setBackground(rounded(bg, size / 2, bg));
    view.setLayoutParams(new LinearLayout.LayoutParams(size, size));
    return view;
  }

  private Button primaryButton(String value) {
    Button button = new Button(this);
    button.setAllCaps(false);
    button.setText(value);
    button.setTextSize(14);
    button.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
    button.setTextColor(Color.WHITE);
    button.setBackground(rounded(ACCENT, 8, ACCENT));
    return button;
  }

  private Button secondaryButton(String value) {
    Button button = new Button(this);
    button.setAllCaps(false);
    button.setText(value);
    button.setTextSize(14);
    button.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
    button.setTextColor(ACCENT_DARK);
    button.setBackground(rounded(ACCENT_SOFT, 8, ACCENT_SOFT));
    return button;
  }

  private Button smallIconButton(String value) {
    Button button = new Button(this);
    button.setAllCaps(false);
    button.setText(value);
    button.setTextSize(12);
    button.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
    button.setTextColor(ACCENT_DARK);
    button.setBackground(rounded(Color.rgb(247, 243, 235), 8, LINE));
    return button;
  }

  private Button actionButton(String value, View.OnClickListener listener) {
    Button button = secondaryButton(value);
    button.setOnClickListener(listener);
    return button;
  }

  private LinearLayout card() {
    LinearLayout card = new LinearLayout(this);
    card.setOrientation(LinearLayout.VERTICAL);
    card.setPadding(dp(16), dp(16), dp(16), dp(16));
    card.setBackground(rounded(CARD, 8, LINE));
    return card;
  }

  private LinearLayout metric(String value, String label) {
    LinearLayout metric = new LinearLayout(this);
    metric.setOrientation(LinearLayout.VERTICAL);
    metric.setPadding(dp(12), dp(12), dp(12), dp(12));
    metric.setBackground(rounded(Color.rgb(251, 248, 241), 8, Color.rgb(235, 228, 215)));
    metric.addView(text(value, 16, INK, Typeface.BOLD));
    addWithTop(metric, paragraph(label), 2);
    return metric;
  }

  private View emptyMessage(String heading, String body) {
    LinearLayout box = card();
    box.addView(text(heading, 18, INK, Typeface.BOLD));
    addWithTop(box, paragraph(body), 6);
    return box;
  }

  private GradientDrawable rounded(int color, int radius, int strokeColor) {
    GradientDrawable drawable = new GradientDrawable();
    drawable.setColor(color);
    drawable.setCornerRadius(dp(radius));
    drawable.setStroke(dp(1), strokeColor);
    return drawable;
  }

  private void addWithTop(LinearLayout parent, View child, int topDp) {
    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
    params.topMargin = dp(topDp);
    parent.addView(child, params);
  }

  private void addSpace(LinearLayout parent, int widthDp, int heightDp) {
    View space = new View(this);
    parent.addView(space, new LinearLayout.LayoutParams(dp(widthDp), dp(heightDp)));
  }

  private LinearLayout dialogForm() {
    LinearLayout form = new LinearLayout(this);
    form.setOrientation(LinearLayout.VERTICAL);
    form.setPadding(dp(4), dp(4), dp(4), dp(4));
    return form;
  }

  private ScrollView wrapDialog(LinearLayout form) {
    ScrollView scrollView = new ScrollView(this);
    scrollView.addView(form);
    return scrollView;
  }

  private EditText input(String hint) {
    EditText input = new EditText(this);
    input.setHint(hint);
    input.setSingleLine(true);
    input.setTextSize(15);
    return input;
  }

  private EditText multiline(String hint) {
    EditText input = new EditText(this);
    input.setHint(hint);
    input.setMinLines(3);
    input.setGravity(Gravity.TOP);
    input.setTextSize(15);
    return input;
  }

  private Spinner spinner(List<String> values) {
    Spinner spinner = new Spinner(this);
    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, values);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    spinner.setAdapter(adapter);
    return spinner;
  }

  private CheckBox checkbox(String label, boolean checked) {
    CheckBox checkBox = new CheckBox(this);
    checkBox.setText(label);
    checkBox.setTextColor(INK);
    checkBox.setTextSize(15);
    checkBox.setChecked(checked);
    return checkBox;
  }

  private View field(String label, View... controls) {
    LinearLayout field = new LinearLayout(this);
    field.setOrientation(LinearLayout.VERTICAL);
    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
    params.bottomMargin = dp(12);
    field.setLayoutParams(params);
    field.addView(text(label, 14, INK, Typeface.BOLD));
    for (View control : controls) {
      addWithTop(field, control, 6);
    }
    return field;
  }

  private View detailRow(String label, String value) {
    LinearLayout row = new LinearLayout(this);
    row.setGravity(Gravity.CENTER_VERTICAL);
    row.setPadding(0, dp(8), 0, dp(8));
    row.addView(paragraph(label), new LinearLayout.LayoutParams(0, -2, 1));
    TextView right = text(value, 15, INK, Typeface.BOLD);
    right.setGravity(Gravity.RIGHT);
    row.addView(right, new LinearLayout.LayoutParams(0, -2, 1));
    return row;
  }

  private static class AppState {
    String currentView = "Home";
    String selectedCategory = "All";
    boolean unlocked = true;
    final List<Person> people = new ArrayList<>();
    final List<Record> records = new ArrayList<>();
    final List<DocumentRef> documents = new ArrayList<>();
    final List<Task> tasks = new ArrayList<>();

    static AppState initial() {
      AppState state = new AppState();
      state.people.add(new Person("person_vinaya", "Vinaya Sathyanarayana", "46", "owner", "Husband", "active", "Today"));
      state.people.add(new Person("person_divya", "Divya Jagadish", "42", "co_owner", "Wife", "active", "Today"));
      state.people.add(new Person("person_trusted_steward", "", "", "steward", "", "not_selected", "Not reviewed"));
      state.documents.add(new DocumentRef("doc_identity_folder", "Identity folder", "Reference", "Household", "placeholder"));
      state.documents.add(new DocumentRef("doc_policy_folder", "Insurance policy folder", "Folder", "Future records", "placeholder"));
      state.tasks.add(new Task("task_confirm_spouse_access", "Confirm spouse access",
          "Vinaya and Divya should both be able to open, edit, and review the household continuity map.", true));
      state.tasks.add(new Task("task_choose_steward", "Choose trusted family steward",
          "Pick one family member who can see explicitly shared emergency information.", false));
      state.tasks.add(new Task("task_add_first_record", "Add first continuity record",
          "Start with one bank account, insurance policy, document folder, or contact.", false));
      state.tasks.add(new Task("task_preview_emergency", "Preview emergency kit",
          "Check what the steward would see before inviting anyone.", false));
      return state;
    }

    JSONObject toJson() {
      JSONObject object = new JSONObject();
      try {
        object.put("currentView", currentView);
        object.put("selectedCategory", selectedCategory);
        object.put("unlocked", unlocked);
        object.put("people", Person.toJsonArray(people));
        object.put("records", Record.toJsonArray(records));
        object.put("documents", DocumentRef.toJsonArray(documents));
        object.put("tasks", Task.toJsonArray(tasks));
      } catch (JSONException ignored) {
      }
      return object;
    }

    static AppState fromJson(JSONObject object) throws JSONException {
      AppState state = new AppState();
      state.currentView = object.optString("currentView", "Home");
      state.selectedCategory = object.optString("selectedCategory", "All");
      state.unlocked = object.optBoolean("unlocked", true);
      Person.fromJsonArray(object.optJSONArray("people"), state.people);
      Record.fromJsonArray(object.optJSONArray("records"), state.records);
      DocumentRef.fromJsonArray(object.optJSONArray("documents"), state.documents);
      Task.fromJsonArray(object.optJSONArray("tasks"), state.tasks);
      if (state.people.isEmpty() || state.tasks.isEmpty()) return initial();
      return state;
    }
  }

  private static class Person {
    String id;
    String name;
    String age;
    String role;
    String relationship;
    String inviteStatus;
    String lastReviewed;

    Person(String id, String name, String age, String role, String relationship, String inviteStatus, String lastReviewed) {
      this.id = id;
      this.name = name;
      this.age = age;
      this.role = role;
      this.relationship = relationship;
      this.inviteStatus = inviteStatus;
      this.lastReviewed = lastReviewed;
    }

    JSONObject toJson() throws JSONException {
      JSONObject object = new JSONObject();
      object.put("id", id);
      object.put("name", name);
      object.put("age", age);
      object.put("role", role);
      object.put("relationship", relationship);
      object.put("inviteStatus", inviteStatus);
      object.put("lastReviewed", lastReviewed);
      return object;
    }

    static JSONArray toJsonArray(List<Person> people) throws JSONException {
      JSONArray array = new JSONArray();
      for (Person person : people) array.put(person.toJson());
      return array;
    }

    static void fromJsonArray(JSONArray array, List<Person> people) throws JSONException {
      if (array == null) return;
      for (int i = 0; i < array.length(); i++) {
        JSONObject object = array.getJSONObject(i);
        people.add(new Person(
            object.optString("id"),
            object.optString("name"),
            object.optString("age"),
            object.optString("role"),
            object.optString("relationship"),
            object.optString("inviteStatus"),
            object.optString("lastReviewed")
        ));
      }
    }
  }

  private static class Record {
    String id = "";
    String title = "";
    String category = "";
    String ownerId = "";
    String note = "";
    String instruction = "";
    boolean reviewed = false;
    final List<String> visibility = new ArrayList<>();

    JSONObject toJson() throws JSONException {
      JSONObject object = new JSONObject();
      object.put("id", id);
      object.put("title", title);
      object.put("category", category);
      object.put("ownerId", ownerId);
      object.put("note", note);
      object.put("instruction", instruction);
      object.put("reviewed", reviewed);
      JSONArray array = new JSONArray();
      for (String item : visibility) array.put(item);
      object.put("visibility", array);
      return object;
    }

    static JSONArray toJsonArray(List<Record> records) throws JSONException {
      JSONArray array = new JSONArray();
      for (Record record : records) array.put(record.toJson());
      return array;
    }

    static void fromJsonArray(JSONArray array, List<Record> records) throws JSONException {
      if (array == null) return;
      for (int i = 0; i < array.length(); i++) {
        JSONObject object = array.getJSONObject(i);
        Record record = new Record();
        record.id = object.optString("id");
        record.title = object.optString("title");
        record.category = object.optString("category");
        record.ownerId = object.optString("ownerId");
        record.note = object.optString("note");
        record.instruction = object.optString("instruction");
        record.reviewed = object.optBoolean("reviewed", false);
        JSONArray visibility = object.optJSONArray("visibility");
        if (visibility != null) {
          for (int v = 0; v < visibility.length(); v++) record.visibility.add(visibility.getString(v));
        }
        records.add(record);
      }
    }
  }

  private static class DocumentRef {
    String id;
    String title;
    String type;
    String linkedTo;
    String status;

    DocumentRef() {
    }

    DocumentRef(String id, String title, String type, String linkedTo, String status) {
      this.id = id;
      this.title = title;
      this.type = type;
      this.linkedTo = linkedTo;
      this.status = status;
    }

    JSONObject toJson() throws JSONException {
      JSONObject object = new JSONObject();
      object.put("id", id);
      object.put("title", title);
      object.put("type", type);
      object.put("linkedTo", linkedTo);
      object.put("status", status);
      return object;
    }

    static JSONArray toJsonArray(List<DocumentRef> documents) throws JSONException {
      JSONArray array = new JSONArray();
      for (DocumentRef document : documents) array.put(document.toJson());
      return array;
    }

    static void fromJsonArray(JSONArray array, List<DocumentRef> documents) throws JSONException {
      if (array == null) return;
      for (int i = 0; i < array.length(); i++) {
        JSONObject object = array.getJSONObject(i);
        documents.add(new DocumentRef(
            object.optString("id"),
            object.optString("title"),
            object.optString("type"),
            object.optString("linkedTo"),
            object.optString("status")
        ));
      }
    }
  }

  private static class Task {
    String id;
    String title;
    String detail;
    boolean done;

    Task(String id, String title, String detail, boolean done) {
      this.id = id;
      this.title = title;
      this.detail = detail;
      this.done = done;
    }

    JSONObject toJson() throws JSONException {
      JSONObject object = new JSONObject();
      object.put("id", id);
      object.put("title", title);
      object.put("detail", detail);
      object.put("done", done);
      return object;
    }

    static JSONArray toJsonArray(List<Task> tasks) throws JSONException {
      JSONArray array = new JSONArray();
      for (Task task : tasks) array.put(task.toJson());
      return array;
    }

    static void fromJsonArray(JSONArray array, List<Task> tasks) throws JSONException {
      if (array == null) return;
      for (int i = 0; i < array.length(); i++) {
        JSONObject object = array.getJSONObject(i);
        tasks.add(new Task(
            object.optString("id"),
            object.optString("title"),
            object.optString("detail"),
            object.optBoolean("done", false)
        ));
      }
    }
  }
}
